package com.fixit.model;

/**
 * Abstract base class for all system users (students and staff).
 *
 * OOP concepts:
 *   - Abstraction  : abstract class – cannot instantiate User directly
 *   - Encapsulation: private fields with getter-only access
 *   - Inheritance  : Student and Staff extend this class
 */
public abstract class User {

    // ---- Encapsulation: private fields ----
    private int    id;
    private String name;
    private String password;

    /**
     * @param id       database primary key
     * @param name     login username
     * @param password the user's password (plain text for this demo)
     */
    public User(int id, String name, String password) {
        this.id       = id;
        this.name     = name;
        this.password = password;
    }

    // ---- Abstract methods – each subclass provides its own version ----

    /**
     * @return "STUDENT" or "STAFF" – used for role checks in services.
     *         OOP: polymorphism – same method, different return values.
     */
    public abstract String getRole();

    /**
     * @return a personalised welcome string shown after login.
     *         OOP: polymorphism.
     */
    public abstract String getWelcomeMessage();

    // ---- Getters ----

    /** @return the database id */
    public int getId() { return id; }

    /** @return the username */
    public String getName() { return name; }

    /** @return the password (used only in AuthService/UserDAO) */
    public String getPassword() { return password; }
}
