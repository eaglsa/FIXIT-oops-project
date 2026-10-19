package com.fixit.model;

import java.time.LocalDate;

/**
 * Represents an electrical complaint (e.g. power outage, short circuit).
 *
 * OOP concepts:
 *   - Inheritance  : extends Complaint
 *   - Polymorphism : overrides getAssignedTeam(), getEstimatedResolutionHours(),
 *                    getSafetyNote(), and getType() with electrical-specific behaviour
 */
public class ElectricalComplaint extends Complaint {

    /**
     * @param description  problem description
     * @param location     where the problem is
     * @param raisedBy     user id of the student
     * @param dateReported date filed
     */
    public ElectricalComplaint(String description, String location,
                                int raisedBy, LocalDate dateReported) {
        super(description, location, raisedBy, dateReported);
    }

    // ---- Polymorphism: each method returns electrical-specific data ----

    @Override
    public ComplaintType getType() {
        return ComplaintType.ELECTRICAL;
    }

    @Override
    public String getAssignedTeam() {
        // OOP: polymorphism – same method name as other subclasses, different return value
        return "Electrician team";
    }

    @Override
    public int getEstimatedResolutionHours() {
        // OOP: polymorphism – electrical issues typically resolved in 24 hours
        return 24;
    }

    @Override
    public String getSafetyNote() {
        // OOP: polymorphism – type-specific safety advice
        return "Do not touch exposed wires; switch off the supply if safe.";
    }
}
