package com.jobradar.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.dto.auth.LoginRequest;
import com.jobradar.application.dto.auth.RegisterRequest;
import com.jobradar.application.model.user.Role;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.security.JwtService;
import com.jobradar.application.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Configurable
public class AuthControllerTests {

    @MockitoBean
    private User user;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private final List<User> mockUserDatabase = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockUserDatabase.clear();
        Mockito.when(userRepository.existsByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return mockUserDatabase.stream().anyMatch(u -> u.getEmail().equals(email));
        });

        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            mockUserDatabase.add(user);
            return user;
        });

        Mockito.when(userRepository.findByEmailVerificationToken(anyString())).thenAnswer(invocation -> {
            String emailVerificationToken = invocation.getArgument(0);
            return mockUserDatabase.stream().filter(u -> u.getEmailVerificationToken().equals(emailVerificationToken)).findFirst();
        });

        Mockito.when(userRepository.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return mockUserDatabase.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        });

        Mockito.when(userRepository.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return mockUserDatabase.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        });

        Mockito.when(authenticationManager.authenticate(any(Authentication.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Full Authentication Flow Test:
     * 1. Register a new user
     * Using Mocking to simulate the database operation (existByEmail, save)
     * URL - /api/auth/register
     * PARAM - [RegisterRequest] registerRequest
     * RETURN - [User] user with email verification token
     * 2. Verify the email verification token
     * Using Mocking to simulate Email verification (findByEmailVerificationToken)
     * URL - /api/auth/verify-email
     * PARAM - [String] token
     * RETURN - [Map<String, String>] map with message
     * 3. Login with the registered user
     * Using Mocking to simulate the database operation (findByEmail)
     * URL - /api/auth/login
     * PARAM - [LoginRequest] loginRequest
     * RETURN - [AuthResponse] authResponse with token
     */
    @Test
    void testFullAuthenticationFlow() throws Exception {
        // Register a new user
        RegisterRequest registerRequest = new RegisterRequest("testuser", "testuser", "test@example.com", "password123");

        String registerResponse = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();

        // Verify the email verification token
        String emailVerificationToken = objectMapper.readTree(registerResponse).get("emailVerificationToken").asText();

        mockMvc.perform(get("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON).param("token", emailVerificationToken)).andExpect(status().isOk()).andExpect(jsonPath("$.message").exists());

        // Login with the registered user
        LoginRequest loginDto = new LoginRequest("test@example.com", "password123");

        String loginResponseJson = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponseJson).get("token").asText();
        Assertions.assertTrue(jwtService.isTokenValid(token, "test@example.com"));
    }

    /**
     * User Signup with an existing email should return a 4xx error
     * Adds user in the mock database
     * Using Mocking to simulate the database operation (existByEmail)
     * URL - /api/auth/register
     * PARAM - [RegisterRequest] registerRequest
     * RETURN - [Map<String, String>] map with the error message
     */
    @Test
    void testSignupAlreadyExists() throws Exception {
        User existingUser = new User("existing", "existing", "test@example.com", "password", Role.USER);
        mockUserDatabase.add(existingUser);

        RegisterRequest registerDto = new RegisterRequest("existing", "existing", "test@example.com", "password");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerDto))).andExpect(status().is4xxClientError()).andExpect(jsonPath("$.error").value("User with Email: test@example.com already exists"));
    }
}
