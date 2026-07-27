package com.antoniosandu.mindtrench.service;

import com.antoniosandu.mindtrench.dto.request.ChangePasswordRequest;
import com.antoniosandu.mindtrench.dto.request.DeleteUserRequest;
import com.antoniosandu.mindtrench.dto.request.LoginRequest;
import com.antoniosandu.mindtrench.dto.request.RegisterRequest;
import com.antoniosandu.mindtrench.dto.response.AuthResponse;
import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.exception.AuthenticationException;
import com.antoniosandu.mindtrench.exception.PasswordMismatchException;
import com.antoniosandu.mindtrench.exception.UserNotFoundException;
import com.antoniosandu.mindtrench.exception.UsernameAlreadyExistsException;
import com.antoniosandu.mindtrench.mapper.UserMapper;
import com.antoniosandu.mindtrench.repository.GameRepository;
import com.antoniosandu.mindtrench.repository.UserRepository;
import com.antoniosandu.mindtrench.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       GameRepository gameRepository,
                       BCryptPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists, pick another");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException(
                    "Password mismatch!");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(
                        request.getPassword())
        );

        User saved = userRepository.save(user);

        return UserMapper.toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new AuthenticationException(
                    "Invalid username or password");
        }

        return UserMapper.toAuthResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {

            throw new AuthenticationException(
                    "Invalid Password");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordMismatchException(
                    "Password mismatch");
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);
    }

    public void deleteUser(Long userId, DeleteUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new AuthenticationException(
                    "Invalid password");
        }

        List<Game> games =
                gameRepository.findByUserIdOrderByCreatedAtDesc(userId);

        gameRepository.deleteAll(games);

        userRepository.delete(user);
    }
}
