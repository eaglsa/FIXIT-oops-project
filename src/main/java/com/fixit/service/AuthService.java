package com.fixit.service;

import com.fixit.dao.UserDAO;
import com.fixit.exception.AuthenticationException;
import com.fixit.exception.InvalidComplaintException;
import com.fixit.model.User;

import java.sql.SQLException;

/**
 * Handles login and student registration.
 * Business rules live here; no SQL, no Scanner.
 *
 * Layering rule: calls UserDAO below; called by CLIMenu above.
 */
public class AuthService {

    private final UserDAO userDAO;

    /**
     * @param userDAO the data access object for users
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Validates the username and password against the database.
     *
     * @param name     username typed by the user
     * @param password password typed by the user
     * @return the authenticated User (Student or Staff)
     * @throws AuthenticationException if credentials are wrong
     */
    public User login(String name, String password) throws AuthenticationException {
        try {
            User user = userDAO.findByNameAndPassword(name, password);
            if (user == null) {
                // OOP: throwing a custom checked exception
                throw new AuthenticationException("Invalid username or password. Please try again.");
            }
            return user;
        } catch (SQLException e) {
            throw new AuthenticationException("Database error during login: " + e.getMessage());
        }
    }

    /**
     * Registers a new student account after validating inputs.
     * Rejects blank names, passwords shorter than 4 characters,
     * and names that are already taken.
     *
     * @param name     desired username
     * @param password desired password
     * @return the newly created Student
     * @throws InvalidComplaintException if validation fails
     */
    public User registerStudent(String name, String password) throws InvalidComplaintException {
        // Trim whitespace so " alice " is treated as "alice"
        name     = name.trim();
        password = password.trim();

        if (name.isEmpty()) {
            throw new InvalidComplaintException("Username cannot be blank.");
        }
        if (password.length() < 4) {
            throw new InvalidComplaintException("Password must be at least 4 characters.");
        }

        try {
            if (userDAO.nameExists(name)) {
                throw new InvalidComplaintException("Username '" + name + "' is already taken. Choose another.");
            }
            return userDAO.createStudent(name, password);
        } catch (SQLException e) {
            throw new InvalidComplaintException("Database error during registration: " + e.getMessage());
        }
    }
}
