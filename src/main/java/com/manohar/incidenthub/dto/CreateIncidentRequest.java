package com.manohar.incidenthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateIncidentRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Pattern(
        regexp = "LOW|MEDIUM|HIGH|CRITICAL",
        message = "Severity must be LOW, MEDIUM, HIGH, CRITICAL"
    )
    private String severity;

    public CreateIncidentRequest(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
}
