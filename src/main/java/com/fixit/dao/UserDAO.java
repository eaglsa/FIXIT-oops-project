package com.fixit.dao;

import com.fixit.db.DBConnector;
import com.fixit.model.Staff;
import com.fixit.model.Student;
import com.fixit.model.User;

import java.sql.*;

/**
 * Data Access Object for the users table.
 * All SQL that touches the users table lives here and nowhere else.
 *
 * Layering rule: only the service layer calls this DAO.
 */
public class UserDAO {

    /**
     * Looks up a user by name and password.
     * Returns a Student or Staff object depending on the role column,
     * or null if no matching user is found.
     *
     * Security: uses PreparedStatement – no SQL injection possible.
     *
     * @param name     the username typed at login
     * @param password the password typed at login
     * @return the matching User, or null
     * @throws SQLException if a database error occurs
     */
    public User findByNameAndPassword(String name, String password) throws SQLException {
        String sql = "SELECT id, name, role, password FROM users WHERE name = ? AND password = ?";

        // try-with-resources: PreparedStatement and ResultSet are closed automatically
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int    id   = rs.getInt("id");
                    String role = rs.getString("role");
                    String pwd  = rs.getString("password");

                    // OOP: polymorphism – return the right subclass based on role
                    if ("STAFF".equals(role)) {
                        return new Staff(id, name, pwd);
                    } else {
                        return new Student(id, name, pwd);
                    }
                }
            }
        }
        return null;   // no user found
    }

    /**
     * Checks whether a username is already taken.
     *
     * @param name the username to check
     * @return true if the name exists in the database
     * @throws SQLException if a database error occurs
     */
    public boolean nameExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a new student into the users table and returns the created User.
     *
     * @param name     chosen username
     * @param password chosen password
     * @return the newly created Student object with its generated id
     * @throws SQLException if a database error occurs
     */
    public User createStudent(String name, String password) throws SQLException {
        String sql = "INSERT INTO users (name, role, password) VALUES (?, 'STUDENT', ?)";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, password);
            ps.executeUpdate();

            // Retrieve the auto-generated primary key
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new Student(generatedId, name, password);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated id after user insert.");
    }
}
