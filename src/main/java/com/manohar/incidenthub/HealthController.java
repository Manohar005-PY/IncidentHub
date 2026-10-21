package com.manohar.incidenthub;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;


@RestController 
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, String> health()
    {
        return Map.of(
            "status","UP",
            "application","IncidentHub API"
        );
    }
}