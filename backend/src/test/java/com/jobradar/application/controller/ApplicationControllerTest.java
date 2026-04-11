package com.jobradar.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void shouldReturnAllApplications() throws Exception {
        Application request = new Application();
        request.setId(1L);
        request.setCompany("Google");
        request.setPosition("Java Dev");
        request.setStatus(ApplicationStatus.APPLIED);

        Application savedApplication = new Application();
        savedApplication.setId(1L);
        savedApplication.setCompany("Google");
        savedApplication.setPosition("Java Dev");
        savedApplication.setStatus(ApplicationStatus.APPLIED);

        when(applicationService.createApplication(any(Application.class)))
                .thenReturn(savedApplication);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.company").value("Google"))
                .andExpect(jsonPath("$.position").value("Java Dev"))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }
}
