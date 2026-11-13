package com.manohar.incidenthub.exception;

public class InvalidRegistrationRequestException extends RuntimeException {

    public InvalidRegistrationRequestException(String message) {
        super(message);
    }
}