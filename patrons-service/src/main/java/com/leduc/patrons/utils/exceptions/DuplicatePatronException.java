package com.leduc.patrons.utils.exceptions;

public class DuplicatePatronException extends RuntimeException {
    public DuplicatePatronException(String message) {
        super(message);
    }
}
