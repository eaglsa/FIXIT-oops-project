package com.fixit.dao;

import com.fixit.db.DBConnector;
import com.fixit.model.Complaint;
import com.fixit.model.ComplaintType;
import com.fixit.model.Status;
import com.fixit.service.ComplaintFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the complaints and status_log tables.
 * All SQL that touches these tables lives here and nowhere else.
 *
 * Layering rule: only the service layer calls this DAO.
 */
public class ComplaintDAO {

    /**
     * Inserts a new complaint into the database and sets the generated id on the object.
     *
     * @param c the Complaint to save (id will be set after insert)
     * @return the generated primary key
     * @throws SQLException if a database error occurs
     */
    public int insert(Complaint c) throws SQLException {
        String sql = "INSERT INTO complaints (type, description, location, status, raised_by, date_reported) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getType().name());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getLocation());
            ps.setString(4, c.getStatus().name());
            ps.setInt(5,    c.getRaisedBy());
            ps.setDate(6,   Date.valueOf(c.getDateReported()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    c.setId(id);   // set the DB-generated id on the model object
                    return id;
                }
            }
        }
        throw new SQLException("Failed to get generated key after complaint insert.");
    }

    /**
     * Loads a single complaint by its primary key.
     *
     * @param id the complaint id
     * @return the Complaint, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Complaint findById(int id) throws SQLException {
        String sql = "SELECT id, type, description, location, status, raised_by, date_reported "
                   + "FROM complaints WHERE id = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns all complaints raised by a specific user.
     *
     * @param userId the user's id
     * @return list of complaints (may be empty)
     * @throws SQLException if a database error occurs
     */
    public List<Complaint> findByUser(int userId) throws SQLException {
        String sql = "SELECT id, type, description, location, status, raised_by, date_reported "
                   + "FROM complaints WHERE raised_by = ? ORDER BY id DESC";
        return queryList(sql, userId);
    }

    /**
     * Returns every complaint in the system (staff view).
     *
     * @return list of all complaints
     * @throws SQLException if a database error occurs
     */
    public List<Complaint> findAll() throws SQLException {
        String sql = "SELECT id, type, description, location, status, raised_by, date_reported "
                   + "FROM complaints ORDER BY id DESC";

        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Returns all complaints with a given status.
     *
     * @param status the status to filter by
     * @return filtered list
     * @throws SQLException if a database error occurs
     */
    public List<Complaint> findByStatus(Status status) throws SQLException {
        String sql = "SELECT id, type, description, location, status, raised_by, date_reported "
                   + "FROM complaints WHERE status = ? ORDER BY id DESC";
        return queryList(sql, status.name());
    }

    /**
     * Updates the complaint status AND inserts a status_log row, all in one transaction.
     * If anything fails, the whole operation is rolled back.
     *
     * @param complaintId the id of the complaint to update
     * @param newStatus   the new status to set
     * @param changedBy   the staff user's id who is making the change
     * @throws SQLException if a database error occurs or the rollback fails
     */
    public void updateStatus(int complaintId, Status newStatus, int changedBy) throws SQLException {
        // First we need to read the current status to log it
        Complaint current = findById(complaintId);
        if (current == null) {
            throw new SQLException("Complaint with id " + complaintId + " not found.");
        }
        Status oldStatus = current.getStatus();

        String updateSql = "UPDATE complaints SET status = ? WHERE id = ?";
        String logSql    = "INSERT INTO status_log (complaint_id, old_status, new_status, changed_by) "
                         + "VALUES (?, ?, ?, ?)";

        Connection conn = DBConnector.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);   // begin transaction

            // 1. Update the complaint row
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus.name());
                ps.setInt(2, complaintId);
                ps.executeUpdate();
            }

            // 2. Insert a status_log row
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, complaintId);
                ps.setString(2, oldStatus.name());
                ps.setString(3, newStatus.name());
                ps.setInt(4, changedBy);
                ps.executeUpdate();
            }

            conn.commit();   // both steps succeeded – commit

        } catch (SQLException e) {
            conn.rollback(); // something failed – undo everything
            throw e;
        } finally {
            conn.setAutoCommit(true);  // restore default mode
        }
    }

    /**
     * Returns a formatted history of all status changes for a complaint.
     * Each line looks like: "2026-10-20 14:32 | REPORTED -> IN_PROGRESS | by warden"
     *
     * @param complaintId the complaint whose history to fetch
     * @return list of formatted history lines
     * @throws SQLException if a database error occurs
     */
    public List<String> getStatusHistory(int complaintId) throws SQLException {
        String sql = "SELECT sl.changed_on, sl.old_status, sl.new_status, u.name "
                   + "FROM status_log sl "
                   + "JOIN users u ON u.id = sl.changed_by "
                   + "WHERE sl.complaint_id = ? "
                   + "ORDER BY sl.changed_on ASC";

        List<String> lines = new ArrayList<>();
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Format: "2026-10-20 14:32 | REPORTED -> IN_PROGRESS | by warden"
                    String when   = rs.getTimestamp("changed_on").toLocalDateTime()
                                      .toString().replace("T", " ").substring(0, 16);
                    String oldSt  = rs.getString("old_status");
                    String newSt  = rs.getString("new_status");
                    String byUser = rs.getString("name");

                    lines.add(when + " | " + oldSt + " -> " + newSt + " | by " + byUser);
                }
            }
        }
        return lines;
    }

    // ---- Private helpers ----

    /**
     * Executes a query with one parameter (int or String) and returns all matching rows.
     */
    private List<Complaint> queryList(String sql, Object param) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param instanceof Integer) {
                ps.setInt(1, (Integer) param);
            } else {
                ps.setString(1, param.toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Maps the current ResultSet row to a Complaint object via ComplaintFactory.
     * Factory pattern: the DAO does not instantiate subclasses directly.
     */
    private Complaint mapRow(ResultSet rs) throws SQLException {
        int           id          = rs.getInt("id");
        ComplaintType type        = ComplaintType.valueOf(rs.getString("type"));
        String        description = rs.getString("description");
        String        location    = rs.getString("location");
        Status        status      = Status.valueOf(rs.getString("status"));
        int           raisedBy    = rs.getInt("raised_by");
        LocalDate     date        = rs.getDate("date_reported").toLocalDate();

        // Use the Factory to reconstruct the right subclass (polymorphism)
        return ComplaintFactory.rebuild(id, type, description, location, raisedBy, date, status);
    }
}
