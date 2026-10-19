package com.fixit.model;

import java.time.LocalDate;

/**
 * Represents a generic / miscellaneous complaint (e.g. broken furniture, pest control).
 *
 * OOP concepts:
 *   - Inheritance  : extends Complaint
 *   - Polymorphism : overrides all abstract methods with generic-maintenance behaviour
 */
public class GenericComplaint extends Complaint {

    /**
     * @param description  problem description
     * @param location     where the problem is
     * @param raisedBy     user id of the student
     * @param dateReported date filed
     */
    public GenericComplaint(String description, String location,
                             int raisedBy, LocalDate dateReported) {
        super(description, location, raisedBy, dateReported);
    }

    // ---- Polymorphism: each method returns generic-maintenance-specific data ----

    @Override
    public ComplaintType getType() {
        return ComplaintType.GENERIC;
    }

    @Override
    public String getAssignedTeam() {
        // OOP: polymorphism – general maintenance team for everything else
        return "General maintenance";
    }

    @Override
    public int getEstimatedResolutionHours() {
        // OOP: polymorphism – generic issues have the longest estimate, 72 hours
        return 72;
    }

    @Override
    public String getSafetyNote() {
        // OOP: polymorphism – no type-specific hazard
        return "No special precautions.";
    }
}
