package com.fixit.service;

import com.fixit.model.*;
import java.time.LocalDate;

/**
 * Factory class that creates the correct Complaint subclass for a given type.
 *
 * OOP concept: Factory pattern – the caller does not need to know which
 * subclass to instantiate; they just pass a ComplaintType and get back
 * a fully-constructed Complaint object.
 */
public class ComplaintFactory {

    // Private constructor – this class should never be instantiated
    private ComplaintFactory() {}

    /**
     * Creates a brand-new complaint (not yet saved to the database).
     * Status defaults to REPORTED, id defaults to 0.
     *
     * OOP: Factory – uses a switch to pick the right subclass.
     *
     * @param type         category of the complaint
     * @param description  problem description
     * @param location     where the problem is
     * @param raisedBy     user id of the student
     * @param date         date the complaint is filed
     * @return the appropriate Complaint subclass instance
     */
    public static Complaint create(ComplaintType type, String description,
                                   String location, int raisedBy, LocalDate date) {
        // OOP: switch on the type to select the right subclass
        return switch (type) {
            case ELECTRICAL -> new ElectricalComplaint(description, location, raisedBy, date);
            case PLUMBING   -> new PlumbingComplaint(description, location, raisedBy, date);
            case WIFI       -> new WifiComplaint(description, location, raisedBy, date);
            case GENERIC    -> new GenericComplaint(description, location, raisedBy, date);
        };
    }

    /**
     * Rebuilds a Complaint object from a database row (includes id and status).
     * Used by ComplaintDAO when reading rows from the DB.
     *
     * @param id           the primary key from the DB
     * @param type         the stored complaint type
     * @param description  stored description
     * @param location     stored location
     * @param raisedBy     stored user id
     * @param date         stored date_reported
     * @param status       the current status read from the DB
     * @return a fully populated Complaint subclass instance
     */
    public static Complaint rebuild(int id, ComplaintType type, String description,
                                    String location, int raisedBy,
                                    LocalDate date, Status status) {
        // First create the complaint with default REPORTED status
        Complaint c = create(type, description, location, raisedBy, date);
        // Then set the real id and status from the database
        c.setId(id);
        c.setStatus(status);
        return c;
    }
}
