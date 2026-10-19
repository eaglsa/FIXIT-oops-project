package com.fixit.exception;

/**
 * Thrown when a complaint has invalid data (empty description,
 * blank location, or an unrecognised category number).
 */
public class InvalidComplaintException extends Exception {

    /** @param message human-readable reason */
    public InvalidComplaintException(String message) {
        super(message);
    }
}
