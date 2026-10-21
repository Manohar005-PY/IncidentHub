package com.manohar.incidenthub;

import org.springframework.web.bind.annotation.RestController;

import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


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
}
