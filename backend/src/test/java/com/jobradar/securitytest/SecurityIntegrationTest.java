package com.jobradar.securitytest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){ userRepository.deleteAll(); }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception{

        RegisterRequest request=new RegisterRequest();
        request.setFirstname("SimpleUserFirstName");
        request.setLastname("SimpleUserLastName");
        request.setEmail("test@example.com");
        request.setPassword("123456");


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname").value("SimpleUserFirstName"))
                .andExpect(jsonPath("$.lastname").value("SimpleUserLastName"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        assertEquals(1, userRepository.count());
    }

}
