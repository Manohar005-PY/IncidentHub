package com.manohar.incidenthub;

import org.springframework.web.bind.annotation.RestController;

import com.manohar.incidenthub.dto.CreateIncidentRequest;
import com.manohar.incidenthub.dto.PageResponse;
import com.manohar.incidenthub.dto.UpdateIncidentStatusRequest;
import com.manohar.incidenthub.exception.InvalidIncidentQueryException;
import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.model.IncidentStatus;
import com.manohar.incidenthub.service.IncidentService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;




@RestController 
public class IncidentController {
    private final IncidentService incidentService;
    private static final Set<String> ALLOWED_SORT_FEILDS = Set.of("id", "title", "severity", "status","version");

    private static final Set<String> ALLOWED_SEVERITY_VALUES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

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
    @PatchMapping("/api/incidents/{id}/status")
    public ResponseEntity <Incident> updateIncidentStatus(@PathVariable Long id, @RequestBody @Valid UpdateIncidentStatusRequest request){
        Incident updated  = incidentService.transitionStatus(id, request.getStatus());

        return ResponseEntity.ok(updated);
        
    }
    @GetMapping("/api/incidents")
    public ResponseEntity<PageResponse<Incident>> getIncidents(
        @RequestParam(required = false) String severity,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy
    ){
        if (page < 0){
            throw new InvalidIncidentQueryException("Page number cannot be negative");
        }
        if (size < 1 || size > 100){
            throw new InvalidIncidentQueryException("Page size must be between 1 and 100");
        }
        if (!ALLOWED_SORT_FEILDS.contains(sortBy)){
            throw new InvalidIncidentQueryException("Invalid sort field: " + sortBy);
        }
        Sort.Direction sortDirection;
        
        try{
            sortDirection = Sort.Direction.fromString("ASC");
        } catch (IllegalArgumentException e){
            throw new InvalidIncidentQueryException("Direction must be ASC or DESC");
        }
        if (severity != null){
            severity = severity.toUpperCase(Locale.ROOT);
            if (!ALLOWED_SEVERITY_VALUES.contains(severity)){
                throw new InvalidIncidentQueryException("Invalid severity value: " + severity);
            }

        }
        IncidentStatus statusFilter = null;
        if (status != null){
            try{
                statusFilter = IncidentStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e){
                throw new InvalidIncidentQueryException("Invalid status value: " + status);
            }
        }
        Pageable pageable = PageRequest.of(page, size, sortDirection, sortBy);

        Page <Incident> result = incidentService.searchIncidents(severity, statusFilter != null ? statusFilter.name() : null, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

}
