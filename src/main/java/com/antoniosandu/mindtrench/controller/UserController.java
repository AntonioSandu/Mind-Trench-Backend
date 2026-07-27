package com.antoniosandu.mindtrench.controller;
import com.antoniosandu.mindtrench.dto.request.ChangePasswordRequest;
import com.antoniosandu.mindtrench.dto.request.DeleteUserRequest;
import com.antoniosandu.mindtrench.dto.request.LoginRequest;
import com.antoniosandu.mindtrench.dto.request.RegisterRequest;
import com.antoniosandu.mindtrench.dto.response.AuthResponse;
import com.antoniosandu.mindtrench.dto.response.MessageResponse;
import com.antoniosandu.mindtrench.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response =
                userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request){

        AuthResponse response =
                userService.login(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(id, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable Long id,
            @Valid @RequestBody DeleteUserRequest request) {

        userService.deleteUser(id, request);

        return ResponseEntity.ok(
                new MessageResponse(
                        "Account deleted successfully"));
    }
}

