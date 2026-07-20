//package com.antoniosandu.mindtrench.controller;
//
//import com.antoniosandu.mindtrench.dto.request.ChangePasswordRequest;
//import com.antoniosandu.mindtrench.dto.request.DeleteUserRequest;
//import com.antoniosandu.mindtrench.dto.request.LoginRequest;
//import com.antoniosandu.mindtrench.exception.AuthenticationException;
//import com.antoniosandu.mindtrench.exception.PasswordMismatchException;
//import com.antoniosandu.mindtrench.exception.UserNotFoundException;
//import com.antoniosandu.mindtrench.exception.UsernameAlreadyExistsException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.antoniosandu.mindtrench.dto.request.RegisterRequest;
//import com.antoniosandu.mindtrench.dto.response.AuthResponse;
//import com.antoniosandu.mindtrench.service.UserService;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//@WebMvcTest(UserController.class)
//class UserControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private UserService userService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void register_ShouldReturn201_WhenValidRequest() throws Exception {
//
//        RegisterRequest request = new RegisterRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//        request.setConfirmPassword("password123");
//
//        AuthResponse response = new AuthResponse();
//        response.setId(1L);
//        response.setUsername("antonio");
//
//        when(userService.register(any(RegisterRequest.class)))
//                .thenReturn(response);
//
//        mockMvc.perform(post("/api/user/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.username").value("antonio"));
//    }
//
//    @Test
//    void register_ShouldReturn409_WhenUsernameAlreadyExists() throws Exception {
//
//        RegisterRequest request = new RegisterRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//        request.setConfirmPassword("password123");
//
//        when(userService.register(any(RegisterRequest.class)))
//                .thenThrow(new UsernameAlreadyExistsException("Username già esistente"));
//
//        mockMvc.perform(post("/api/user/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.message").value("Username già esistente"));
//    }
//
//    @Test
//    void register_ShouldReturn400_WhenPasswordsDoNotMatch() throws Exception {
//
//        RegisterRequest request = new RegisterRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//        request.setConfirmPassword("differentPassword");
//
//        when(userService.register(any(RegisterRequest.class)))
//                .thenThrow(new PasswordMismatchException("Le password non coincidono"));
//
//        mockMvc.perform(post("/api/user/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Le password non coincidono"));
//    }
//
//    @Test
//    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("antonio");
//        request.setPassword("password123");
//
//        AuthResponse response = new AuthResponse();
//        response.setId(1L);
//        response.setUsername("antonio");
//
//        when(userService.login(any(LoginRequest.class)))
//                .thenReturn(response);
//
//        mockMvc.perform(post("/api/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.username").value("antonio"));
//    }
//
//    @Test
//    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("antonio");
//        request.setPassword("wrongPassword");
//
//        when(userService.login(any(LoginRequest.class)))
//                .thenThrow(new AuthenticationException("Username o password non validi"));
//
//        mockMvc.perform(post("/api/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.message").value("Username o password non validi"));
//    }
//
//    @Test
//    void changePassword_ShouldReturn200_WhenRequestIsValid() throws Exception {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("oldPassword");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("newPassword");
//
//        mockMvc.perform(patch("/api/user/{id}/password", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void changePassword_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("oldPassword");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("newPassword");
//
//        doThrow(new UserNotFoundException("Utente non trovato"))
//                .when(userService)
//                .changePassword(anyLong(), any(ChangePasswordRequest.class));
//
//        mockMvc.perform(patch("/api/user/{id}/password", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Utente non trovato"));
//    }
//
//    @Test
//    void changePassword_ShouldReturn401_WhenCurrentPasswordIsWrong() throws Exception {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("wrongOld");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("newPassword");
//
//        doThrow(new AuthenticationException("Password attuale non valida"))
//                .when(userService)
//                .changePassword(anyLong(), any(ChangePasswordRequest.class));
//
//        mockMvc.perform(patch("/api/user/{id}/password", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.message").value("Password attuale non valida"));
//    }
//
//    @Test
//    void changePassword_ShouldReturn400_WhenNewPasswordsDoNotMatch() throws Exception {
//
//        Long userId = 1L;
//
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setCurrentPassword("oldPassword");
//        request.setNewPassword("newPassword");
//        request.setConfirmNewPassword("differentPassword");
//
//        doThrow(new PasswordMismatchException("Le nuove password non coincidono"))
//                .when(userService)
//                .changePassword(anyLong(), any(ChangePasswordRequest.class));
//
//        mockMvc.perform(patch("/api/user/{id}/password", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("Le nuove password non coincidono"));
//    }
//
//    @Test
//    void deleteUser_ShouldReturn200_WhenRequestIsValid() throws Exception {
//
//        Long userId = 1L;
//
//        DeleteUserRequest request = new DeleteUserRequest();
//        request.setPassword("password123");
//
//        mockMvc.perform(delete("/api/user/{id}", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message")
//                        .value("Account deleted successfully"));
//    }
//
//    @Test
//    void deleteUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
//
//        Long userId = 1L;
//
//        DeleteUserRequest request = new DeleteUserRequest();
//        request.setPassword("password123");
//
//        doThrow(new UserNotFoundException("Utente non trovato"))
//                .when(userService)
//                .deleteUser(anyLong(), any(DeleteUserRequest.class));
//
//        mockMvc.perform(delete("/api/user/{id}", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message")
//                        .value("Utente non trovato"));
//    }
//
//    @Test
//    void deleteUser_ShouldReturn401_WhenPasswordIsWrong() throws Exception {
//
//        Long userId = 1L;
//
//        DeleteUserRequest request = new DeleteUserRequest();
//        request.setPassword("wrongPassword");
//
//        doThrow(new AuthenticationException("Password non valida"))
//                .when(userService)
//                .deleteUser(anyLong(), any(DeleteUserRequest.class));
//
//        mockMvc.perform(delete("/api/user/{id}", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.message")
//                        .value("Password non valida"));
//    }
//
//}
