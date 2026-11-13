package com.manohar.incidenthub.mapper;

import com.manohar.incidenthub.dto.UserResponse;
import com.manohar.incidenthub.model.AppUser;

public class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole());
    }
}
