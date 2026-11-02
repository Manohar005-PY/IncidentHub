package com.manohar.incidenthub.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.manohar.incidenthub.exception.IncidentNotFoundException;
import com.manohar.incidenthub.exception.InvalidStatusTransitionException;
import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.model.IncidentStatus;
import com.manohar.incidenthub.repositroy.IncidentRepositroy;

@Service
public class IncidentService {
    private final IncidentRepositroy incidentRepositroy;

    public IncidentService(IncidentRepositroy incidentRepositroy) {
        this.incidentRepositroy = incidentRepositroy;
    }

    public Optional<Incident> getIncidentById(Long id) {
        return incidentRepositroy.findById(id);
    }

    public Incident createIncident(String title, String severity) {
        Incident incident = new Incident(title, severity, IncidentStatus.OPEN);

        return incidentRepositroy.save(incident);
    }

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
        return incidentRepositroy.save(incident);
    }
}
