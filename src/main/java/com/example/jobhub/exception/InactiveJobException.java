package com.example.jobhub.exception;

public class InactiveJobException extends RuntimeException {

    public InactiveJobException() {
        super("This job is no longer accepting applications.");
    }
}
