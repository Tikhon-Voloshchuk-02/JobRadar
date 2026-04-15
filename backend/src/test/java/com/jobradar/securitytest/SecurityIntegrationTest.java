package com.jobradar.securitytest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.controller.AuthController;
import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.exception.EmailAlreadyExistsException;
import com.jobradar.application.model.user.Role;
import com.jobradar.application.model.user.User;
import com.jobradar.application.security.JwtAuthenticationFilter;
import com.jobradar.application.security.JwtService;
import com.jobradar.application.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SecurityIntegrationTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    // Test successful user registration and verifies that correct user data is returned with HTTP-201 status
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstname("SimpleUserFirstName");
        request.setLastname("SimpleUserLastName");
        request.setEmail("test@example.com");
        request.setPassword("123456");

        User savedUser = new User();
        savedUser.setFirstname("SimpleUserFirstName");
        savedUser.setLastname("SimpleUserLastName");
        savedUser.setEmail("test@example.com");
        savedUser.setRole(Role.USER);

        when(authService.register(any(RegisterRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname").value("SimpleUserFirstName"))
                .andExpect(jsonPath("$.lastname").value("SimpleUserLastName"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }


    // Tests registration failure when email already exists and verifies HTTP-409 response with error-message
    @Test
    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstname("SimpleUserFirstName");
        request.setLastname("SimpleUserLastName");
        request.setEmail("test@example.com");
        request.setPassword("123456");

        doThrow(new EmailAlreadyExistsException("test@example.com"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error")
                        .value("User with Email: test@example.com already exists"));
    }
}
