package com.fixit.model;

/**
 * Workflow states a complaint can be in.
 *
 * OOP concept: Enum with behaviour – the canTransitionTo() method
 * encodes the transition rules so they live in one place.
 *
 * Allowed transitions:
 *   REPORTED  -> IN_PROGRESS
 *   IN_PROGRESS -> RESOLVED
 * Everything else (skipping, backwards, staying the same) is invalid.
 */
public enum Status {

    REPORTED,
    IN_PROGRESS,
    RESOLVED;

    /**
     * Checks whether moving to {@code next} is a valid step in the workflow.
     *
     * @param next the desired new status
     * @return true only when the transition is allowed
     */
    public boolean canTransitionTo(Status next) {
        // OOP: behaviour inside an enum (not just a plain constant)
        return switch (this) {
            case REPORTED    -> next == IN_PROGRESS;   // only one legal next step
            case IN_PROGRESS -> next == RESOLVED;      // only one legal next step
            case RESOLVED    -> false;                 // terminal state – no further changes
        };
    }
}
