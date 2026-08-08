package com.example.jobhub.exception;

public class ForbiddenOwnershipException extends RuntimeException {

    public ForbiddenOwnershipException() {
        super("You do not have permission to access this record.");
    }
}
