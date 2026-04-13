package com.jobradar.application.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.exception.EmailAlreadyExistsException;
import com.jobradar.application.model.user.Role;
import com.jobradar.application.model.user.User;

import com.jobradar.application.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.jobradar.application.exception.GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register should create user and return 201")
    void register_ShouldReturnCreatedUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstname("Tikhon");
        request.setLastname("Voloshchuk");
        request.setEmail("tikhon@example.com");
        request.setPassword("123456");

        User savedUser = new User();
        savedUser.setFirstname("Tikhon");
        savedUser.setLastname("Voloshchuk");
        savedUser.setEmail("tikhon@example.com");
        savedUser.setRole(Role.USER);

        when(authService.register(any(RegisterRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
               // .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Tikhon"))
                .andExpect(jsonPath("$.lastname").value("Voloshchuk"))
                .andExpect(jsonPath("$.email").value("tikhon@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }



    @Test
    @DisplayName("POST /api/auth/register should return 409 when email already exists")
    void register_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstname("Tikhon");
        request.setLastname("Voloshchuk");
        request.setEmail("tikhon@example.com");
        request.setPassword("123456");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("tikhon@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("User with Email: tikhon@example.com already exists"));
    }


}
