package com.manohar.incidenthub.dto;

import com.manohar.incidenthub.model.UserRole;

public record UserResponse(
        Long id,
        String email,
        UserRole role) {
}