package com.fixit.model;

/**
 * A student user – can raise complaints and view their own complaints.
 *
 * OOP concepts:
 *   - Inheritance  : extends User
 *   - Polymorphism : implements getRole() and getWelcomeMessage() differently from Staff
 */
public class Student extends User {

    /**
     * @param id       database primary key
     * @param name     login username
     * @param password the student's password
     */
    public Student(int id, String name, String password) {
        super(id, name, password);
    }

    @Override
    public String getRole() {
        // OOP: polymorphism – returns "STUDENT", different from Staff which returns "STAFF"
        return "STUDENT";
    }

    @Override
    public String getWelcomeMessage() {
        // OOP: polymorphism – student-specific welcome text
        return "Welcome, Student " + getName() + "! You can raise and track your complaints.";
    }
}
