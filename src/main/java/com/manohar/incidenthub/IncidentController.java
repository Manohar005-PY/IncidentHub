package com.manohar.incidenthub;

import org.springframework.web.bind.annotation.RestController;

import com.manohar.incidenthub.dto.CreateIncidentRequest;
import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.service.IncidentService;

import jakarta.validation.Valid;

import java.net.URI;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
public class IncidentController {
    private final IncidentService incidentService;

    @Autowired
    public IncidentController(IncidentService incidentService)
    {
        this.incidentService = incidentService;
    }

    @GetMapping("/api/incident/{id}")
    public ResponseEntity<Incident> getIncident(@PathVariable  Long id)
    {   
        
       var result = incidentService.getIncidentById(id);
       if (result.isEmpty()){
        return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(result.get());
    }

    @PostMapping("/api/incidents")
    public ResponseEntity <Incident> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        Incident created = incidentService.createIncident(request.getTitle(), request.getSeverity());

        URI location = URI.create("/api/incidents/" + created.getId());

        return ResponseEntity.created(location).body(created);
    }
    
}
