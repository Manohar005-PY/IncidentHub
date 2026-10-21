package com.manohar.incidenthub.repositroy;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manohar.incidenthub.model.Incident;

public interface IncidentRepositroy 
    extends JpaRepository<Incident, Long> {
}
