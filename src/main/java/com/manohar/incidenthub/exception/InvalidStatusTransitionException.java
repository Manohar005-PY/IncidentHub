package com.manohar.incidenthub.exception;

import com.manohar.incidenthub.model.IncidentStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(IncidentStatus currentStatus, IncidentStatus newStatus) {
        super("Invalid status transition from " + currentStatus + " to " + newStatus);
    }
}
