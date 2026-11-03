package com.manohar.incidenthub.repositroy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manohar.incidenthub.model.Incident;

public interface IncidentRepositroy 
    extends JpaRepository<Incident, Long> {

        @Query("""
            SELECT i
            FROM Incident i
            WHERE (:severity IS NULL OR i.severity = :severity)AND (:status IS NULL OR i.status = :status)""")
        
        Page<Incident> searchIncidents(
            @Param("severity") String severity,
            @Param("status") String status,
            Pageable pageable
        );
}
