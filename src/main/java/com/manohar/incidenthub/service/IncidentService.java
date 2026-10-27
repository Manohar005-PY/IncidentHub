package com.manohar.incidenthub.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.manohar.incidenthub.model.Incident;
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

    public Incident createIncident(String title,String severity){
        Incident incident = new Incident(title, severity, "OPEN");

        return incidentRepositroy.save(incident);
    }
}
