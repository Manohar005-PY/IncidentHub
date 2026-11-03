package com.manohar.incidenthub.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.manohar.incidenthub.exception.IncidentNotFoundException;
import com.manohar.incidenthub.exception.InvalidStatusTransitionException;
import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.model.IncidentStatus;
import com.manohar.incidenthub.repositroy.IncidentRepositroy;

import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {
    private final IncidentRepositroy incidentRepositroy;

    public IncidentService(IncidentRepositroy incidentRepositroy) {
        this.incidentRepositroy = incidentRepositroy;
    }
    @Transactional(readOnly = true)
    public Optional<Incident> getIncidentById(Long id) {
        return incidentRepositroy.findById(id);
    }

    @Transactional 
    public Incident createIncident(String title, String severity) {
        Incident incident = new Incident(title, severity, IncidentStatus.OPEN);

        incident.setStatus(IncidentStatus.OPEN);
        return incidentRepositroy.save(incident);
    }

    @Transactional
    public Incident transitionStatus(Long id, IncidentStatus newStatus) {
        Incident incident = incidentRepositroy.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));

        IncidentStatus currentStatus = incident.getStatus();
        boolean allowed = switch (currentStatus) {
            case OPEN -> newStatus == IncidentStatus.ACKNOWLEDGED;
            case ACKNOWLEDGED -> newStatus == IncidentStatus.INVESTIGATING;
            case INVESTIGATING -> newStatus == IncidentStatus.RESOLVED;
            case RESOLVED -> newStatus == IncidentStatus.OPEN;
        };

        if (!allowed) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }
        incident.setStatus(newStatus);
        return incident;
    }
    @Transactional(readOnly = true)
    public Page<Incident> searchIncidents(String severity, String status, Pageable pageable) {
        return incidentRepositroy.searchIncidents(severity, status, pageable);
    }
}
