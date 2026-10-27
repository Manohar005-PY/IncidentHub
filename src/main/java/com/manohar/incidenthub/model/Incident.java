package com.manohar.incidenthub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    protected Incident() {
        
    }

    public Incident(String title, String severity, IncidentStatus status) {
        this.title = title;
        this.severity = severity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }
}