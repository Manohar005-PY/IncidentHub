package com.manohar.incidenthub.dto;

import java.time.Instant;

import com.manohar.incidenthub.model.IncidentStatus;

public record IncidentResponse(
        Long id,
        String title,
        String severity,
        IncidentStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
