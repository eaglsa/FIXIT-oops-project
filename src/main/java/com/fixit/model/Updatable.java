package com.fixit.model;

import com.fixit.exception.InvalidStatusTransitionException;

/**
 * Any object that can have its status updated must implement this interface.
 *
 * OOP concept: Interface – defines a contract that Complaint must fulfil.
 */
public interface Updatable {

    /**
     * Change the status of this object to {@code newStatus}.
     *
     * @param newStatus the desired next status
     * @throws InvalidStatusTransitionException if the transition is not allowed
     */
    void updateStatus(Status newStatus) throws InvalidStatusTransitionException;
}
