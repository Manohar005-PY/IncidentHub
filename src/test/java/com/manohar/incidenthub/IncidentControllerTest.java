package com.manohar.incidenthub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.manohar.incidenthub.service.IncidentService;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @Test
    void shouldRejectBlankTitle() throws Exception {

        mockMvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "   ",
                        "severity": "HIGH"
                    }
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void shouldRejectInvalidSeverity() throws Exception {

        mockMvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Database connection failed",
                        "severity": "URGENT"
                    }
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }
}