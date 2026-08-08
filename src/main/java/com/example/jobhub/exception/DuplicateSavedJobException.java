package com.example.jobhub.exception;

public class DuplicateSavedJobException extends RuntimeException {

    public DuplicateSavedJobException() {
        super("This job is already saved.");
    }
}
