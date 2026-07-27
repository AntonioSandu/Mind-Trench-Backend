//package com.antoniosandu.mindtrench.service;
//
//import com.antoniosandu.mindtrench.dto.request.ChangePasswordRequest;
//import com.antoniosandu.mindtrench.dto.request.DeleteUserRequest;
//import com.antoniosandu.mindtrench.dto.request.LoginRequest;
//import com.antoniosandu.mindtrench.dto.request.RegisterRequest;
//import com.antoniosandu.mindtrench.dto.response.AuthResponse;
//import com.antoniosandu.mindtrench.entity.Game;
//import com.antoniosandu.mindtrench.entity.User;
//import com.antoniosandu.mindtrench.exception.AuthenticationException;
//import com.antoniosandu.mindtrench.exception.PasswordMismatchException;
//import com.antoniosandu.mindtrench.exception.UserNotFoundException;
//import com.antoniosandu.mindtrench.exception.UsernameAlreadyExistsException;
//import com.antoniosandu.mindtrench.repository.GameRepository;
//import com.antoniosandu.mindtrench.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private GameRepository gameRepository;
//
//    @Mock
//    private BCryptPasswordEncoder passwordEncoder;
//
//    @InjectMocks
//    private UserService userService;
//
//    @Test
//    void register_ShouldReturnAuthResponse_WhenDataAreValid() {
//
//        RegisterRequest request = new RegisterRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//        request.setConfirmPassword("password123");
//
//        User savedUser = new User(
//                "antonio",
//                "encodedPassword");
//
//        when(userRepository.existsByUsername("antonio"))
//                .thenReturn(false);
//
//        when(passwordEncoder.encode("password123"))
//                .thenReturn("encodedPassword");
//
//        when(userRepository.save(any(User.class)))
//                .thenReturn(savedUser);
//
//        AuthResponse response = userService.register(request);
//
//        assertNotNull(response);
//        assertEquals("antonio", response.getUsername());
//
//        ArgumentCaptor<User> userCaptor =
//                ArgumentCaptor.forClass(User.class);
//
//        verify(userRepository).save(userCaptor.capture());
//
//        User capturedUser = userCaptor.getValue();
//
//        assertEquals("antonio", capturedUser.getUsername());
//        assertEquals("encodedPassword", capturedUser.getPasswordHash());
//        assertEquals(0, capturedUser.getBestEndlessScore());
//    }
//
//    @Test
//    void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameExists() {
//
//        RegisterRequest request = new RegisterRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//        request.setConfirmPassword("password123");
//
//        when(userRepository.existsByUsername("antonio"))
//                .thenReturn(true);
//
//        assertThrows(
//                UsernameAlreadyExistsException.class,
//                () -> userService.register(request)
//        );
//
//        verify(userRepository).existsByUsername("antonio");
//
//        verify(userRepository, never()).save(any(User.class));
//        verify(passwordEncoder, never()).encode(anyString());
//    }
//
//    @Test
//    void register_ShouldThrowPasswordMismatchException_WhenPasswordsDoNotMatch() {
//
//        RegisterRequest request = new RegisterRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//        request.setConfirmPassword("password456");
//
//        when(userRepository.existsByUsername("antonio"))
//                .thenReturn(false);
//
//        assertThrows(
//                PasswordMismatchException.class,
//                () -> userService.register(request)
//        );
//
//        verify(userRepository).existsByUsername("antonio");
//
//        verify(passwordEncoder, never()).encode(anyString());
//        verify(userRepository, never()).save(any(User.class));
//
//        verifyNoMoreInteractions(userRepository, passwordEncoder);
//    }
//
//    @Test
//    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//
//        User user = new User("antonio", "encodedPassword");
//
//        when(userRepository.findByUsername("antonio"))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("password123", "encodedPassword"))
//                .thenReturn(true);
//
//        AuthResponse response = userService.login(request);
//
//        assertNotNull(response);
//        assertEquals("antonio", response.getUsername());
//
//        verify(userRepository).findByUsername("antonio");
//        verify(passwordEncoder).matches("password123", "encodedPassword");
//    }
//
//    @Test
//    void login_ShouldThrowAuthenticationException_WhenUserDoesNotExist() {
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//
//        when(userRepository.findByUsername("antonio"))
//                .thenReturn(Optional.empty());
//
//        assertThrows(
//                AuthenticationException.class,
//                () -> userService.login(request)
//        );
//
//        verify(userRepository).findByUsername("antonio");
//        verify(passwordEncoder, never()).matches(anyString(), anyString());
//    }
//
//    @Test
//    void login_ShouldThrowAuthenticationException_WhenPasswordIsWrong() {
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("antonio");
//        request.setPassword("wrongPassword");
//
//        User user = new User("antonio", "encodedPassword");
//
//        when(userRepository.findByUsername("antonio"))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
//                .thenReturn(false);
//
//        assertThrows(
//                AuthenticationException.class,
//                () -> userService.login(request)
//        );
//
//        verify(userRepository).findByUsername("antonio");
//        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
//    }
//
//    @Test
//    void changePassword_ShouldUpdatePassword_WhenDataAreValid() {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("oldPassword");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("newPassword");
//
//        User user = new User("antonio", "encodedOldPassword");
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("oldPassword", "encodedOldPassword"))
//                .thenReturn(true);
//
//        when(passwordEncoder.encode("newPassword"))
//                .thenReturn("encodedNewPassword");
//
//        userService.changePassword(userId, request);
//
//        verify(userRepository).findById(userId);
//        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
//        verify(passwordEncoder).encode("newPassword");
//        verify(userRepository).save(user);
//
//        assertEquals("encodedNewPassword", user.getPasswordHash());
//    }
//
//    @Test
//    void changePassword_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.empty());
//
//        assertThrows(
//                UserNotFoundException.class,
//                () -> userService.changePassword(userId, request)
//        );
//
//        verify(userRepository).findById(userId);
//        verifyNoMoreInteractions(userRepository);
//        verifyNoInteractions(passwordEncoder);
//    }
//
//    @Test
//    void changePassword_ShouldThrowAuthenticationException_WhenCurrentPasswordIsWrong() {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("wrongOld");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("newPassword");
//
//        User user = new User("antonio", "encodedOldPassword");
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("wrongOld", "encodedOldPassword"))
//                .thenReturn(false);
//
//        assertThrows(
//                AuthenticationException.class,
//                () -> userService.changePassword(userId, request)
//        );
//
//        verify(userRepository).findById(userId);
//        verify(passwordEncoder).matches("wrongOld", "encodedOldPassword");
//
//        verify(userRepository, never()).save(any());
//    }
//
//    @Test
//    void changePassword_ShouldThrowPasswordMismatchException_WhenNewPasswordsDoNotMatch() {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("oldPassword");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("differentPassword");
//
//        User user = new User("antonio", "encodedOldPassword");
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("oldPassword", "encodedOldPassword"))
//                .thenReturn(true);
//
//        assertThrows(
//                PasswordMismatchException.class,
//                () -> userService.changePassword(userId, request)
//        );
//
//        verify(userRepository).findById(userId);
//        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
//
//        verify(userRepository, never()).save(any());
//    }
//
//    @Test
//    void deleteUser_ShouldDeleteUserAndAllGames_WhenPasswordIsCorrect() {
//
//        Long userId = 1L;
//
//        DeleteUserRequest request = new DeleteUserRequest();
//        request.setPassword("password123");
//
//        User user = new User("antonio", "encodedPassword");
//
//        List<Game> games = List.of(
//                new Game(),
//                new Game()
//        );
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches(
//                "password123",
//                "encodedPassword"))
//                .thenReturn(true);
//
//        when(gameRepository.findByUserIdOrderByCreatedAtDesc(userId))
//                .thenReturn(games);
//
//        userService.deleteUser(userId, request);
//
//        verify(userRepository).findById(userId);
//
//        verify(passwordEncoder)
//                .matches("password123", "encodedPassword");
//
//        verify(gameRepository)
//                .findByUserIdOrderByCreatedAtDesc(userId);
//
//        verify(gameRepository)
//                .deleteAll(games);
//
//        verify(userRepository)
//                .delete(user);
//    }
//
//    @Test
//    void deleteUser_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
//
//        Long userId = 1L;
//
//        DeleteUserRequest request = new DeleteUserRequest();
//        request.setPassword("password123");
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.empty());
//
//        assertThrows(
//                UserNotFoundException.class,
//                () -> userService.deleteUser(userId, request)
//        );
//
//        verify(userRepository).findById(userId);
//        verifyNoInteractions(passwordEncoder);
//        verify(userRepository, never()).delete(any());
//    }
//
//    @Test
//    void deleteUser_ShouldThrowAuthenticationException_WhenPasswordIsWrong() {
//
//        Long userId = 1L;
//
//        DeleteUserRequest request = new DeleteUserRequest();
//        request.setPassword("wrongPassword");
//
//        User user = new User("antonio", "encodedPassword");
//
//        when(userRepository.findById(userId))
//                .thenReturn(Optional.of(user));
//
//        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
//                .thenReturn(false);
//
//        assertThrows(
//                AuthenticationException.class,
//                () -> userService.deleteUser(userId, request)
//        );
//
//        verify(userRepository).findById(userId);
//        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
//        verify(userRepository, never()).delete(any());
//    }
//
//}
