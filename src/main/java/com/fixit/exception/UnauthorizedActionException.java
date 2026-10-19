package com.fixit.exception;

/**
 * Thrown when a user tries an action they are not permitted to perform,
 * e.g. a student attempting to update a complaint status.
 */
public class UnauthorizedActionException extends Exception {

    /** @param message human-readable reason */
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
