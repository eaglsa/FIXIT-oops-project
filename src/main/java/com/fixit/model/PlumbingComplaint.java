package com.fixit.model;

import java.time.LocalDate;

/**
 * Represents a plumbing complaint (e.g. leaking pipe, blocked drain).
 *
 * OOP concepts:
 *   - Inheritance  : extends Complaint
 *   - Polymorphism : overrides all abstract methods with plumbing-specific behaviour
 */
public class PlumbingComplaint extends Complaint {

    /**
     * @param description  problem description
     * @param location     where the problem is
     * @param raisedBy     user id of the student
     * @param dateReported date filed
     */
    public PlumbingComplaint(String description, String location,
                              int raisedBy, LocalDate dateReported) {
        super(description, location, raisedBy, dateReported);
    }

    // ---- Polymorphism: each method returns plumbing-specific data ----

    @Override
    public ComplaintType getType() {
        return ComplaintType.PLUMBING;
    }

    @Override
    public String getAssignedTeam() {
        // OOP: polymorphism – different team for plumbing
        return "Plumbing team";
    }

    @Override
    public int getEstimatedResolutionHours() {
        // OOP: polymorphism – plumbing takes longer, 48 hours estimated
        return 48;
    }

    @Override
    public String getSafetyNote() {
        // OOP: polymorphism – plumbing-specific safety advice
        return "Turn off the nearest valve if there is flooding.";
    }
}
