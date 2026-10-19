package com.fixit.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages the single database connection for the entire application.
 *
 * OOP concept: Singleton pattern
 *   - Only ONE instance of DBConnector can ever exist.
 *   - All parts of the application share the same connection.
 *   - This avoids opening multiple connections and wastes fewer resources.
 *
 * How the Singleton works:
 *   1. The constructor is private, so nobody outside can call "new DBConnector()".
 *   2. The single instance is stored in a private static field.
 *   3. getInstance() creates it the first time and returns it every time after.
 */
public class DBConnector {

    // The one-and-only instance (starts as null until first call to getInstance())
    private static DBConnector instance;

    // The live JDBC connection
    private Connection connection;

    // ---- Singleton: private constructor ----
    /**
     * Loads db.properties from the classpath and opens a MySQL connection.
     * Called only once by getInstance().
     */
    private DBConnector() {
        try {
            // Load the properties file from src/main/resources/
            InputStream input = getClass().getClassLoader()
                                          .getResourceAsStream("db.properties");

            if (input == null) {
                System.err.println("[ERROR] db.properties not found in classpath.");
                System.err.println("        Copy db.properties.example to");
                System.err.println("        src/main/resources/db.properties");
                System.err.println("        and fill in your MySQL password.");
                return;
            }

            Properties props = new Properties();
            props.load(input);

            String url      = props.getProperty("db.url");
            String user     = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            this.connection = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            System.err.println("[ERROR] Could not connect to MySQL: " + e.getMessage());
            System.err.println("        Make sure MySQL is running and db.properties");
            System.err.println("        has the correct password.");
        }
    }

    // ---- Singleton: public static accessor ----
    /**
     * Returns the single shared instance of DBConnector,
     * creating it on the first call.
     *
     * @return the DBConnector singleton
     */
    public static DBConnector getInstance() {
        if (instance == null) {
            instance = new DBConnector();   // created exactly once
        }
        return instance;
    }

    /**
     * Returns the live JDBC connection.
     * If the connection was closed (e.g. after a timeout), it reconnects.
     *
     * @return an open Connection, or null if the connection could not be established
     */
    public Connection getConnection() {
        try {
            // Reconnect if the connection was dropped
            if (connection == null || connection.isClosed()) {
                instance = new DBConnector();
                return instance.connection;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection check failed: " + e.getMessage());
        }
        return connection;
    }
}
