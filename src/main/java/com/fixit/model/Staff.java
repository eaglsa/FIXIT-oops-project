package com.fixit.model;

/**
 * A staff user – can view all complaints and update their status.
 *
 * OOP concepts:
 *   - Inheritance  : extends User
 *   - Polymorphism : implements getRole() and getWelcomeMessage() differently from Student
 */
public class Staff extends User {

    /**
     * @param id       database primary key
     * @param name     login username
     * @param password the staff member's password
     */
    public Staff(int id, String name, String password) {
        super(id, name, password);
    }

    @Override
    public String getRole() {
        // OOP: polymorphism – returns "STAFF", different from Student which returns "STUDENT"
        return "STAFF";
    }

    @Override
    public String getWelcomeMessage() {
        // OOP: polymorphism – staff-specific welcome text
        return "Welcome, Staff " + getName() + "! You can manage and resolve complaints.";
    }
}
