package com.fixit.exception;

/**
 * Thrown when login fails due to an incorrect username or password.
 */
public class AuthenticationException extends Exception {

    /** @param message human-readable reason */
    public AuthenticationException(String message) {
        super(message);
    }
}
