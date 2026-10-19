package com.fixit.model;

import java.time.LocalDate;

/**
 * Represents a Wi-Fi / network complaint (e.g. no internet, slow speeds).
 *
 * OOP concepts:
 *   - Inheritance  : extends Complaint
 *   - Polymorphism : overrides all abstract methods with Wi-Fi-specific behaviour
 */
public class WifiComplaint extends Complaint {

    /**
     * @param description  problem description
     * @param location     where the problem is
     * @param raisedBy     user id of the student
     * @param dateReported date filed
     */
    public WifiComplaint(String description, String location,
                          int raisedBy, LocalDate dateReported) {
        super(description, location, raisedBy, dateReported);
    }

    // ---- Polymorphism: each method returns Wi-Fi-specific data ----

    @Override
    public ComplaintType getType() {
        return ComplaintType.WIFI;
    }

    @Override
    public String getAssignedTeam() {
        // OOP: polymorphism – network team handles Wi-Fi issues
        return "Network/IT team";
    }

    @Override
    public int getEstimatedResolutionHours() {
        // OOP: polymorphism – Wi-Fi issues are quickest to fix, 12 hours
        return 12;
    }

    @Override
    public String getSafetyNote() {
        // OOP: polymorphism – no physical danger, just a diagnostic tip
        return "Try reconnecting before reporting again.";
    }
}
