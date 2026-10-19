package com.fixit;

import com.fixit.dao.ComplaintDAO;
import com.fixit.dao.UserDAO;
import com.fixit.db.DBConnector;
import com.fixit.service.AuthService;
import com.fixit.service.ComplaintService;
import com.fixit.ui.CLIMenu;

import java.sql.Connection;

/**
 * Application entry point.
 *
 * Wires up all layers (db -> dao -> service -> ui) and starts the CLI menu.
 * If the database connection fails, a clear error is printed and the app exits.
 */
public class Main {

    public static void main(String[] args) {

        // ---- 1. Check DB connection before doing anything else ----
        Connection conn = DBConnector.getInstance().getConnection();
        if (conn == null) {
            System.err.println();
            System.err.println("======================================================");
            System.err.println("  STARTUP ERROR: Could not connect to the database.");
            System.err.println("  Please check:");
            System.err.println("  1. MySQL is running.");
            System.err.println("  2. src/main/resources/db.properties has the correct");
            System.err.println("     URL, username, and password.");
            System.err.println("  3. You have run sql/schema.sql to create the schema.");
            System.err.println("======================================================");
            System.exit(1);
        }

        // ---- 2. Wire up the layers (Dependency Injection by hand) ----
        UserDAO          userDAO          = new UserDAO();
        ComplaintDAO     complaintDAO     = new ComplaintDAO();
        AuthService      authService      = new AuthService(userDAO);
        ComplaintService complaintService = new ComplaintService(complaintDAO);

        // ---- 3. Hand control to the UI ----
        CLIMenu menu = new CLIMenu(authService, complaintService);
        menu.start();
    }
}
