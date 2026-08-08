package com.example.jobhub.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException() {
        super("You have already applied for this job.");
    }
}
