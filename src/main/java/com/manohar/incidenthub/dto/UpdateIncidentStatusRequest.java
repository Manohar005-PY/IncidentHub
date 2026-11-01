package com.manohar.incidenthub.dto;

import com.manohar.incidenthub.model.IncidentStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateIncidentStatusRequest {

    @NotNull(message = "Status cannot be null")
    private IncidentStatus status;

    public UpdateIncidentStatusRequest() {
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public IncidentStatus getStatus() {
        return status;
    }
}
