package com.manohar.incidenthub.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manohar.incidenthub.dto.RegisterRequest;
import com.manohar.incidenthub.dto.UserResponse;
import com.manohar.incidenthub.mapper.UserMapper;
import com.manohar.incidenthub.model.AppUser;
import com.manohar.incidenthub.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = authService.register(
                request.getEmail(),
                request.getPassword());

        UserResponse response = UserMapper.toResponse(user);

        URI location = URI.create("/api/users/" + user.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}
