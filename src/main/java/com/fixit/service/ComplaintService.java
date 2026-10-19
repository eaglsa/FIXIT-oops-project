package com.fixit.service;

import com.fixit.dao.ComplaintDAO;
import com.fixit.exception.InvalidComplaintException;
import com.fixit.exception.InvalidStatusTransitionException;
import com.fixit.exception.UnauthorizedActionException;
import com.fixit.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Business logic for raising and managing complaints.
 * No SQL here – delegates to ComplaintDAO.
 * No Scanner or System.out – delegates up to CLIMenu.
 *
 * Layering rule: calls ComplaintDAO below; called by CLIMenu above.
 */
public class ComplaintService {

    private final ComplaintDAO complaintDAO;

    /**
     * @param complaintDAO the data access object for complaints
     */
    public ComplaintService(ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    /**
     * Creates and saves a new complaint. Only students may raise complaints.
     *
     * @param user        the logged-in user (must be a Student)
     * @param type        the complaint category
     * @param description text describing the problem (min 5 chars)
     * @param location    where the problem is (non-blank)
     * @return the saved Complaint with its database id set
     * @throws UnauthorizedActionException if the user is not a Student
     * @throws InvalidComplaintException   if description or location fails validation
     */
    public Complaint raiseComplaint(User user, ComplaintType type,
                                    String description, String location)
            throws UnauthorizedActionException, InvalidComplaintException {

        // Only students can raise complaints
        if (!"STUDENT".equals(user.getRole())) {
            throw new UnauthorizedActionException("Only students can raise complaints.");
        }

        // Trim and validate inputs
        description = description.trim();
        location    = location.trim();

        if (description.length() < 5) {
            throw new InvalidComplaintException("Description must be at least 5 characters.");
        }
        if (location.isEmpty()) {
            throw new InvalidComplaintException("Location cannot be blank.");
        }

        // OOP: Factory creates the right subclass; polymorphism happens at runtime
        Complaint c = ComplaintFactory.create(type, description, location,
                                              user.getId(), LocalDate.now());
        try {
            complaintDAO.insert(c);   // id is set inside insert()
        } catch (SQLException e) {
            throw new InvalidComplaintException("Database error while saving complaint: " + e.getMessage());
        }
        return c;
    }

    /**
     * Returns all complaints raised by the logged-in student.
     *
     * @param user the logged-in user
     * @return the student's complaints
     * @throws UnauthorizedActionException if user is not a student
     */
    public List<Complaint> getMyComplaints(User user) throws UnauthorizedActionException {
        if (!"STUDENT".equals(user.getRole())) {
            throw new UnauthorizedActionException("This action is for students only.");
        }
        try {
            return complaintDAO.findByUser(user.getId());
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not load complaints: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Returns all complaints in the system (staff only).
     *
     * @param user the logged-in user (must be Staff)
     * @return all complaints
     * @throws UnauthorizedActionException if user is not staff
     */
    public List<Complaint> getAllComplaints(User user) throws UnauthorizedActionException {
        if (!"STAFF".equals(user.getRole())) {
            throw new UnauthorizedActionException("Only staff can view all complaints.");
        }
        try {
            return complaintDAO.findAll();
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not load complaints: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Returns all complaints filtered by status (staff only).
     *
     * @param user   the logged-in user (must be Staff)
     * @param status the status to filter by
     * @return filtered complaints
     * @throws UnauthorizedActionException if user is not staff
     */
    public List<Complaint> getComplaintsByStatus(User user, Status status)
            throws UnauthorizedActionException {
        if (!"STAFF".equals(user.getRole())) {
            throw new UnauthorizedActionException("Only staff can filter complaints by status.");
        }
        try {
            return complaintDAO.findByStatus(status);
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not load complaints: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Updates the status of a complaint (staff only).
     * Validates the transition using the Complaint model before persisting.
     *
     * @param user        the logged-in user (must be Staff)
     * @param complaintId the id of the complaint to update
     * @param newStatus   the desired new status
     * @throws UnauthorizedActionException       if user is not staff
     * @throws InvalidComplaintException         if the complaint id does not exist
     * @throws InvalidStatusTransitionException  if the transition is not allowed
     */
    public void updateStatus(User user, int complaintId, Status newStatus)
            throws UnauthorizedActionException, InvalidComplaintException,
                   InvalidStatusTransitionException {

        if (!"STAFF".equals(user.getRole())) {
            throw new UnauthorizedActionException("Only staff can update complaint status.");
        }

        try {
            Complaint c = complaintDAO.findById(complaintId);
            if (c == null) {
                throw new InvalidComplaintException("No complaint found with id " + complaintId + ".");
            }

            // OOP: calls Complaint.updateStatus() which validates via Status.canTransitionTo()
            // If invalid, InvalidStatusTransitionException is thrown here
            c.updateStatus(newStatus);

            // Persist the change and log it in a single transaction
            complaintDAO.updateStatus(complaintId, newStatus, user.getId());

        } catch (SQLException e) {
            throw new InvalidComplaintException("Database error while updating status: " + e.getMessage());
        }
    }

    /**
     * Fetches a single complaint. Students can only view their own; staff can view any.
     *
     * @param user the logged-in user
     * @param id   the complaint id
     * @return the Complaint
     * @throws UnauthorizedActionException if a student tries to view another student's complaint
     * @throws InvalidComplaintException   if the complaint doesn't exist
     */
    public Complaint getComplaintDetails(User user, int id)
            throws UnauthorizedActionException, InvalidComplaintException {
        try {
            Complaint c = complaintDAO.findById(id);
            if (c == null) {
                throw new InvalidComplaintException("No complaint found with id " + id + ".");
            }

            // Students can only view their own complaints
            if ("STUDENT".equals(user.getRole()) && c.getRaisedBy() != user.getId()) {
                throw new UnauthorizedActionException("You can only view your own complaints.");
            }

            return c;
        } catch (SQLException e) {
            throw new InvalidComplaintException("Database error: " + e.getMessage());
        }
    }

    /**
     * Returns the status-change history for a complaint.
     * Students can only view history for their own complaints; staff can view any.
     *
     * @param user        the logged-in user
     * @param complaintId the complaint id
     * @return list of formatted history lines
     * @throws UnauthorizedActionException if access is denied
     * @throws InvalidComplaintException   if the complaint doesn't exist
     */
    public List<String> getHistory(User user, int complaintId)
            throws UnauthorizedActionException, InvalidComplaintException {
        // Re-use getComplaintDetails() for access-control check
        getComplaintDetails(user, complaintId);

        try {
            return complaintDAO.getStatusHistory(complaintId);
        } catch (SQLException e) {
            throw new InvalidComplaintException("Database error while loading history: " + e.getMessage());
        }
    }

    /**
     * Returns a count of complaints grouped by status (staff dashboard).
     *
     * @param user the logged-in user (must be Staff)
     * @return map of Status -> count
     * @throws UnauthorizedActionException if user is not staff
     */
    public Map<Status, Integer> getSummaryCounts(User user) throws UnauthorizedActionException {
        if (!"STAFF".equals(user.getRole())) {
            throw new UnauthorizedActionException("Only staff can view the dashboard.");
        }
        Map<Status, Integer> counts = new LinkedHashMap<>();
        // Initialise all statuses to 0 so the dashboard always shows all three
        for (Status s : Status.values()) {
            counts.put(s, 0);
        }
        try {
            List<Complaint> all = complaintDAO.findAll();
            for (Complaint c : all) {
                counts.merge(c.getStatus(), 1, Integer::sum);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not load dashboard counts: " + e.getMessage());
        }
        return counts;
    }
}
