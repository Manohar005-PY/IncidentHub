package com.manohar.incidenthub;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.manohar.incidenthub.exception.IncidentNotFoundException;
import com.manohar.incidenthub.exception.InvalidStatusTransitionException;
import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.model.IncidentStatus;
import com.manohar.incidenthub.service.IncidentService;

import jakarta.transaction.InvalidTransactionException;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    // @MockitoBean
    // private IncidentNotFoundException incidentNotFoundException;

    @MockitoBean
    private InvalidTransactionException invalidTransactionException;

    @Test
    void shouldListIncidentsSuccessfully() throws Exception {
        Incident incident = new Incident(
                "Database connection failed",
                "HIGH",
                IncidentStatus.OPEN);
        incident.setId(1L);
        when(incidentService.searchIncidents(
                isNull(),
                isNull(),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")))))
                .thenReturn(new PageImpl<>(List.of(incident),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")), 1));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Database connection failed"))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(incidentService).searchIncidents(
                isNull(),
                isNull(),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Test
    void shouldListIncidentsWithSeverityFilter() throws Exception {
        when(incidentService.searchIncidents(
                eq("HIGH"),
                isNull(),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")))))
                .thenReturn(new PageImpl<>(List.of(),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")), 0));

        mockMvc.perform(get("/api/incidents").param("severity", "HIGH"))
                .andExpect(status().isOk());

        verify(incidentService).searchIncidents(
                eq("HIGH"),
                isNull(),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Test
    void shouldListIncidentsWithStatusFilter() throws Exception {
        when(incidentService.searchIncidents(
                isNull(),
                eq("ACKNOWLEDGED"),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")))))
                .thenReturn(new PageImpl<>(List.of(),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")), 0));

        mockMvc.perform(get("/api/incidents").param("status", "ACKNOWLEDGED"))
                .andExpect(status().isOk());

        verify(incidentService).searchIncidents(
                isNull(),
                eq("ACKNOWLEDGED"),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Test
    void shouldRejectNegativePageNumber() throws Exception {
        mockMvc.perform(get("/api/incidents").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void shouldRejectPageSizeGreaterThan100() throws Exception {
        mockMvc.perform(get("/api/incidents").param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void shouldRejectInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/incidents").param("sortBy", "createdAt"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void shouldRejectInvalidSeverityFilter() throws Exception {
        mockMvc.perform(get("/api/incidents").param("severity", "URGENT"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void shouldRejectInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/api/incidents").param("status", "CLOSED"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

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

    @Test
    void shouldUpdateIncidentStatus() throws Exception {
        Incident incident = new Incident(
                "Database connection failed",
                "HIGH",
                IncidentStatus.ACKNOWLEDGED);
        incident.setId(1L);
        when(incidentService.transitionStatus(1L, IncidentStatus.ACKNOWLEDGED)).thenReturn(incident);

        mockMvc.perform(patch("/api/incidents/1/status").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "status": "ACKNOWLEDGED"
                }
                """)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    }

    @Test
    void shouldReturnBadRequestWhenMissingStatus() throws Exception {

        mockMvc.perform(patch("/api/incidents/1/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void cheakingErrorMessageWhenIncidentNotFound() throws Exception {

        when(incidentService.transitionStatus(1L, IncidentStatus.ACKNOWLEDGED))
                .thenThrow(new IncidentNotFoundException(1L));

        mockMvc.perform(patch("/api/incidents/1/status").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "status": "ACKNOWLEDGED"}
                """)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Incident with id 1 not found"));
    }

    @Test
    void cheakingErrorMessageWhenInvalidTransaction() throws Exception {

        when(incidentService.transitionStatus(1L, IncidentStatus.RESOLVED))
                .thenThrow(new InvalidStatusTransitionException(IncidentStatus.OPEN, IncidentStatus.RESOLVED));

        mockMvc.perform(patch("/api/incidents/1/status").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "status": "RESOLVED"}
                """)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status transition from OPEN to RESOLVED"));
    }
}