package com.fixit.model;

import com.fixit.exception.InvalidStatusTransitionException;
import java.time.LocalDate;

/**
 * Abstract base class for all complaint types.
 *
 * OOP concepts demonstrated here:
 *   - Abstraction  : abstract class with abstract methods
 *   - Encapsulation: all fields are private; accessed via getters/setters
 *   - Interface    : implements Updatable
 *   - Inheritance  : subclasses extend this class
 */
public abstract class Complaint implements Updatable {

    // ---- Encapsulation: private fields ----
    private int       id;            // set after DB insert (0 means not yet saved)
    private String    description;
    private String    location;
    private Status    status;
    private int       raisedBy;      // the user id of the student who raised it
    private LocalDate dateReported;

    /**
     * Constructor used by ComplaintFactory when creating a new complaint.
     *
     * @param description  text describing the problem
     * @param location     where the problem is (e.g. "Block A, Room 203")
     * @param raisedBy     user id of the student
     * @param dateReported date the complaint was filed
     */
    public Complaint(String description, String location, int raisedBy, LocalDate dateReported) {
        this.description  = description;
        this.location     = location;
        this.raisedBy     = raisedBy;
        this.dateReported = dateReported;
        this.status       = Status.REPORTED;  // every new complaint starts as REPORTED
        this.id           = 0;
    }

    // ----------------------------------------------------------------
    // Updatable interface implementation
    // ----------------------------------------------------------------

    /**
     * Validates the transition using Status.canTransitionTo(), then applies it.
     * Throws if the transition is not allowed.
     *
     * OOP: implements the Updatable interface contract.
     */
    @Override
    public void updateStatus(Status newStatus) throws InvalidStatusTransitionException {
        if (!this.status.canTransitionTo(newStatus)) {
            // OOP: throwing a custom checked exception
            throw new InvalidStatusTransitionException(this.status.name(), newStatus.name());
        }
        this.status = newStatus;
    }

    // ----------------------------------------------------------------
    // Abstract methods – each subclass MUST provide its own version
    // (this is the polymorphism point examiners look for)
    // ----------------------------------------------------------------

    /** @return the complaint category (ELECTRICAL, PLUMBING, WIFI, GENERIC) */
    public abstract ComplaintType getType();

    /**
     * @return the team responsible for handling this type of complaint.
     *         OOP: polymorphism – same method name, different behaviour per subclass.
     */
    public abstract String getAssignedTeam();

    /**
     * @return estimated hours to resolve, which varies by type.
     *         OOP: polymorphism.
     */
    public abstract int getEstimatedResolutionHours();

    /**
     * @return a short safety instruction relevant to this complaint type.
     *         OOP: polymorphism.
     */
    public abstract String getSafetyNote();

    // ----------------------------------------------------------------
    // Concrete method – shared by all subclasses
    // ----------------------------------------------------------------

    /**
     * Returns a formatted summary string for display on the CLI.
     *
     * @return a multi-line block with id, type, location, status, date, description
     */
    public String getSummary() {
        return "  ID          : " + id + "\n"
             + "  Type        : " + getType().getDisplayName() + "\n"
             + "  Location    : " + location + "\n"
             + "  Status      : " + status + "\n"
             + "  Date        : " + dateReported + "\n"
             + "  Description : " + description;
    }

    // ----------------------------------------------------------------
    // Getters (no setters for immutable fields)
    // ----------------------------------------------------------------

    /** @return the database primary key (0 if not yet saved) */
    public int getId() { return id; }

    /** @return the description text */
    public String getDescription() { return description; }

    /** @return the location string */
    public String getLocation() { return location; }

    /** @return the current workflow status */
    public Status getStatus() { return status; }

    /** @return the user id of the student who raised this complaint */
    public int getRaisedBy() { return raisedBy; }

    /** @return the date this complaint was first filed */
    public LocalDate getDateReported() { return dateReported; }

    /**
     * Sets the database id. Called by ComplaintDAO after inserting a new row.
     *
     * @param id the generated primary key from the DB
     */
    public void setId(int id) { this.id = id; }

    /**
     * Directly sets the status (used by ComplaintDAO when loading from DB).
     * Does NOT validate the transition – use updateStatus() for validated changes.
     *
     * @param status the status value read from the database
     */
    public void setStatus(Status status) { this.status = status; }
}
