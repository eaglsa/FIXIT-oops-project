package com.fixit.exception;

/**
 * Thrown when a status transition is not allowed by the workflow rules,
 * e.g. trying to go from REPORTED directly to RESOLVED.
 */
public class InvalidStatusTransitionException extends Exception {

    /**
     * @param from the current status
     * @param to   the requested status
     */
    public InvalidStatusTransitionException(String from, String to) {
        super("Cannot change status from " + from + " to " + to
                + ". Allowed path: REPORTED -> IN_PROGRESS -> RESOLVED.");
    }
}
