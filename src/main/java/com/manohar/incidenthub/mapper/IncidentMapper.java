package com.manohar.incidenthub.mapper;

import com.manohar.incidenthub.dto.IncidentResponse;
import com.manohar.incidenthub.model.Incident;

public final class IncidentMapper {

    private IncidentMapper() {
    }

    public static IncidentResponse toResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getUpdatedAt());
    }
}
