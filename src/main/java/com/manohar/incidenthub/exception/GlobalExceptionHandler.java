package com.manohar.incidenthub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.manohar.incidenthub.dto.ApiErrorResponse;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIncidentNotFoundException(IncidentNotFoundException ex) {
        ApiErrorResponse error = new ApiErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStatusTransitionException(InvalidStatusTransitionException ex){
        ApiErrorResponse error = new ApiErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
