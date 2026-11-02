package com.manohar.incidenthub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manohar.incidenthub.exception.IncidentNotFoundException;
import com.manohar.incidenthub.exception.InvalidStatusTransitionException;
import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.model.IncidentStatus;
import com.manohar.incidenthub.repositroy.IncidentRepositroy;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {
    @Mock
    private IncidentRepositroy incidentRepository;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void shouldReturnIncidentWhenExists() {
        Incident expected = new Incident("payment service is unavilable", "HIGH", IncidentStatus.OPEN);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(expected));

        Optional<Incident> result = incidentService.getIncidentById(1L);

        assertTrue(result.isPresent());
        assertEquals("payment service is unavilable", result.get().getTitle());
        assertEquals(IncidentStatus.OPEN, result.get().getStatus());
    }

    @Test
    void shouldReturnEmptyWhenIncidentDoesNotExist() {

        when(incidentRepository.findById(546L)).thenReturn(Optional.empty());

        Optional<Incident> result = incidentService.getIncidentById(546L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCreateIncidentWithOpenStatus() {
        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident created = incidentService.createIncident("Database connection failed", "HIGH");

        assertNotNull(created);
        assertEquals("Database connection failed", created.getTitle());
        assertEquals("HIGH", created.getSeverity());
        assertEquals(IncidentStatus.OPEN, created.getStatus());

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldChangeIncidentStatusToAcknowledged() {
        Incident expected = new Incident("payment service is unavialble", "HIGH", IncidentStatus.OPEN);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(expected));

        Incident incident = incidentService.transitionStatus(1L, IncidentStatus.ACKNOWLEDGED);

        assertEquals(IncidentStatus.ACKNOWLEDGED, incident.getStatus());
    }

    @Test
    void shouldThrowExceptionForInvaidStatusTransition(){
        Incident expected  = new Incident("payment service is unavialble", "HIGH", IncidentStatus.OPEN);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(expected));

        InvalidStatusTransitionException exception = assertThrows(InvalidStatusTransitionException.class, () -> incidentService.transitionStatus(1L, IncidentStatus.RESOLVED));

        assertTrue(exception.getMessage().contains("Invalid status transition"));

        verify(incidentRepository,never()).save(any(Incident.class));

    }
    @Test 
    void shouldThrowExceptionWhenIncidentNotFoundForStatusTransition(){
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());

        IncidentNotFoundException exception = assertThrows(IncidentNotFoundException.class, () -> incidentService.transitionStatus(1L, IncidentStatus.ACKNOWLEDGED));

        assertTrue(exception.getMessage().contains("Incident with id 1 not found"));

        verify(incidentRepository,never()).save(any(Incident.class));
    }

}
