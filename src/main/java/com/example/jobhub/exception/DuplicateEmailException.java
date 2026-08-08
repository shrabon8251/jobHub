package com.example.jobhub.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("An account already uses that email address.");
    }
}
