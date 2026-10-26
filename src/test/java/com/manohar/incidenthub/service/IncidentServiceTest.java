package com.manohar.incidenthub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.repositroy.IncidentRepositroy;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {
    @Mock
    private IncidentRepositroy incidentRepository;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void shouldReturnIncidentWhenExists(){
        Incident expected = new Incident("Payment Service is Unavilable", "HIGH", "OPEN");

        when(incidentRepository.findById(123L)).thenReturn(Optional.of(expected));
        
        Optional<Incident> result = incidentService.getIncidentById(123L);

        assertTrue(result.isPresent());
        assertEquals("Payment Service is unavilable", result.get().getTitle());
        assertEquals("OPEN", result.get().getStatus());
    }

    @Test 

    void shouldReturnEmptyWhenIncidentDoesNotExist(){

        when(incidentRepository.findById(546L)).thenReturn(Optional.empty());

        Optional<Incident> result = incidentService.getIncidentById(546L);

        assertTrue(result.isEmpty());
    }

}
