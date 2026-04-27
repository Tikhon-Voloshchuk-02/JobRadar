package com.jobradar.securitytest;

import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:jobradar_test",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "google.client-id=test-google-client-id"
})
@AutoConfigureMockMvc
public class ApplicationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void shouldReturnForbiddenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldAllowAccessWithAuthenticatedUser() throws Exception {
        Application app = new Application();
        app.setId(1L);
        app.setCompany("Google");
        app.setPosition("Java Developer");
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationService.getApplications(null, null)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk());
    }
}
