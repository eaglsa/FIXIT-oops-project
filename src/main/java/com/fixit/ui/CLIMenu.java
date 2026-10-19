package com.fixit.ui;

import com.fixit.exception.*;
import com.fixit.model.*;
import com.fixit.service.AuthService;
import com.fixit.service.ComplaintService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The Command-Line Interface for FixIt.
 * This is the ONLY class allowed to use Scanner and System.out.
 *
 * Layering rule: calls AuthService and ComplaintService only.
 *                Never touches DAOs or JDBC directly.
 */
public class CLIMenu {

    // The single Scanner shared across all input reads
    private final Scanner       scanner;
    private final AuthService   authService;
    private final ComplaintService complaintService;

    private static final String SEPARATOR = "--------------------------------------------";

    /**
     * @param authService      for login / register
     * @param complaintService for all complaint operations
     */
    public CLIMenu(AuthService authService, ComplaintService complaintService) {
        this.scanner          = new Scanner(System.in);
        this.authService      = authService;
        this.complaintService = complaintService;
    }

    // ================================================================
    //  Entry point
    // ================================================================

    /**
     * Starts the application loop. Runs until the user chooses Exit.
     */
    public void start() {
        printBanner();
        boolean running = true;

        while (running) {
            printStartMenu();
            int choice = readInt(1, 3);

            switch (choice) {
                case 1 -> doLogin();
                case 2 -> doRegister();
                case 3 -> {
                    System.out.println("Goodbye! Stay safe.");
                    running = false;
                }
            }
        }
    }

    // ================================================================
    //  Start menu actions
    // ================================================================

    /** Handles the login flow. On success routes to the right role menu. */
    private void doLogin() {
        System.out.println(SEPARATOR);
        System.out.println("  LOGIN");
        System.out.println(SEPARATOR);
        System.out.print("Username: ");
        String name = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.login(name, password);
            System.out.println();
            System.out.println(user.getWelcomeMessage());  // OOP: polymorphism
            System.out.println();

            if ("STAFF".equals(user.getRole())) {
                showStaffMenu(user);
            } else {
                showStudentMenu(user);
            }
        } catch (AuthenticationException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    /** Handles student self-registration. */
    private void doRegister() {
        System.out.println(SEPARATOR);
        System.out.println("  REGISTER (Student)");
        System.out.println(SEPARATOR);
        System.out.print("Choose a username: ");
        String name = scanner.nextLine().trim();
        System.out.print("Choose a password (min 4 chars): ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.registerStudent(name, password);
            System.out.println("[OK] Registered successfully! You can now log in as: " + user.getName());
        } catch (InvalidComplaintException e) {
            System.out.println("[!] Registration failed: " + e.getMessage());
        }
    }

    // ================================================================
    //  Student menu
    // ================================================================

    /** Main loop for a logged-in student. */
    private void showStudentMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println(SEPARATOR);
            System.out.println("  STUDENT MENU  [" + user.getName() + "]");
            System.out.println(SEPARATOR);
            System.out.println("  1. Raise a complaint");
            System.out.println("  2. View my complaints");
            System.out.println("  3. View complaint details & history");
            System.out.println("  4. Logout");
            System.out.println(SEPARATOR);
            System.out.print("Choice: ");

            int choice = readInt(1, 4);
            switch (choice) {
                case 1 -> doRaiseComplaint(user);
                case 2 -> doViewMyComplaints(user);
                case 3 -> doViewComplaintDetails(user);
                case 4 -> {
                    System.out.println("Logged out. See you soon!");
                    loggedIn = false;
                }
            }
        }
    }

    /** Guides the student through raising a complaint with a preview step. */
    private void doRaiseComplaint(User user) {
        System.out.println(SEPARATOR);
        System.out.println("  RAISE A COMPLAINT");
        System.out.println(SEPARATOR);
        System.out.println("  Select category:");
        System.out.println("  1. Electrical");
        System.out.println("  2. Plumbing");
        System.out.println("  3. Wi-Fi");
        System.out.println("  4. Generic / Other");
        System.out.print("Choice: ");

        int typeChoice = readInt(1, 4);
        ComplaintType type = ComplaintType.fromMenuChoice(typeChoice);

        System.out.print("Location (e.g. Block A, Room 203): ");
        String location = scanner.nextLine().trim();

        System.out.print("Describe the problem: ");
        String description = scanner.nextLine().trim();

        // Show a preview BEFORE saving – uses polymorphic methods on a temporary object
        // We use ComplaintFactory to build a temporary object just for the preview
        Complaint preview = com.fixit.service.ComplaintFactory.create(
                type, description.isEmpty() ? "preview" : description,
                location.isEmpty()          ? "preview" : location,
                user.getId(), java.time.LocalDate.now());

        System.out.println();
        System.out.println("  --- PREVIEW ---");
        System.out.println("  Type            : " + type.getDisplayName());
        System.out.println("  Location        : " + location);
        System.out.println("  Description     : " + description);
        System.out.println("  Assigned team   : " + preview.getAssignedTeam());       // polymorphism
        System.out.println("  Est. resolution : " + preview.getEstimatedResolutionHours() + " hours");
        System.out.println("  Safety note     : " + preview.getSafetyNote());          // polymorphism
        System.out.println("  ---------------");
        System.out.print("Confirm? (y/n): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Complaint cancelled.");
            return;
        }

        try {
            Complaint saved = complaintService.raiseComplaint(user, type, description, location);
            System.out.println("[OK] Complaint saved with ID: " + saved.getId());
        } catch (UnauthorizedActionException | InvalidComplaintException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    /** Prints all complaints raised by this student. */
    private void doViewMyComplaints(User user) {
        System.out.println(SEPARATOR);
        System.out.println("  MY COMPLAINTS");
        System.out.println(SEPARATOR);
        try {
            List<Complaint> list = complaintService.getMyComplaints(user);
            if (list.isEmpty()) {
                System.out.println("  You have not raised any complaints yet.");
            } else {
                for (Complaint c : list) {
                    System.out.println(c.getSummary());
                    System.out.println(SEPARATOR);
                }
            }
        } catch (UnauthorizedActionException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    /** Shows full details and status history for one complaint. */
    private void doViewComplaintDetails(User user) {
        System.out.print("Enter complaint ID: ");
        int id = readInt(1, Integer.MAX_VALUE);

        try {
            Complaint c = complaintService.getComplaintDetails(user, id);
            System.out.println(SEPARATOR);
            System.out.println(c.getSummary());
            System.out.println("  Assigned team   : " + c.getAssignedTeam());
            System.out.println("  Est. resolution : " + c.getEstimatedResolutionHours() + " hours");
            System.out.println("  Safety note     : " + c.getSafetyNote());
            System.out.println(SEPARATOR);
            System.out.println("  STATUS HISTORY");
            System.out.println(SEPARATOR);

            List<String> history = complaintService.getHistory(user, id);
            if (history.isEmpty()) {
                System.out.println("  No status changes yet.");
            } else {
                for (String line : history) {
                    System.out.println("  " + line);
                }
            }
            System.out.println(SEPARATOR);
        } catch (UnauthorizedActionException | InvalidComplaintException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    // ================================================================
    //  Staff menu
    // ================================================================

    /** Main loop for a logged-in staff member. */
    private void showStaffMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println(SEPARATOR);
            System.out.println("  STAFF MENU  [" + user.getName() + "]");
            System.out.println(SEPARATOR);
            System.out.println("  1. View all complaints");
            System.out.println("  2. View by status");
            System.out.println("  3. Update complaint status");
            System.out.println("  4. View complaint details & history");
            System.out.println("  5. Dashboard (counts per status)");
            System.out.println("  6. Logout");
            System.out.println(SEPARATOR);
            System.out.print("Choice: ");

            int choice = readInt(1, 6);
            switch (choice) {
                case 1 -> doViewAllComplaints(user);
                case 2 -> doViewByStatus(user);
                case 3 -> doUpdateStatus(user);
                case 4 -> doViewComplaintDetails(user);
                case 5 -> doDashboard(user);
                case 6 -> {
                    System.out.println("Logged out. See you soon!");
                    loggedIn = false;
                }
            }
        }
    }

    /** Prints every complaint in the system. */
    private void doViewAllComplaints(User user) {
        System.out.println(SEPARATOR);
        System.out.println("  ALL COMPLAINTS");
        System.out.println(SEPARATOR);
        try {
            List<Complaint> list = complaintService.getAllComplaints(user);
            if (list.isEmpty()) {
                System.out.println("  No complaints in the system yet.");
            } else {
                for (Complaint c : list) {
                    System.out.println(c.getSummary());
                    System.out.println(SEPARATOR);
                }
            }
        } catch (UnauthorizedActionException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    /** Filters complaints by a chosen status. */
    private void doViewByStatus(User user) {
        System.out.println("  Select status to filter:");
        System.out.println("  1. REPORTED");
        System.out.println("  2. IN_PROGRESS");
        System.out.println("  3. RESOLVED");
        System.out.print("Choice: ");
        int choice = readInt(1, 3);
        Status status = switch (choice) {
            case 1 -> Status.REPORTED;
            case 2 -> Status.IN_PROGRESS;
            default -> Status.RESOLVED;
        };

        System.out.println(SEPARATOR);
        System.out.println("  COMPLAINTS with status: " + status);
        System.out.println(SEPARATOR);
        try {
            List<Complaint> list = complaintService.getComplaintsByStatus(user, status);
            if (list.isEmpty()) {
                System.out.println("  None found.");
            } else {
                for (Complaint c : list) {
                    System.out.println(c.getSummary());
                    System.out.println(SEPARATOR);
                }
            }
        } catch (UnauthorizedActionException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    /** Guides staff through updating a complaint status with confirmation. */
    private void doUpdateStatus(User user) {
        System.out.println(SEPARATOR);
        System.out.println("  UPDATE COMPLAINT STATUS");
        System.out.println(SEPARATOR);
        System.out.print("Enter complaint ID: ");
        int id = readInt(1, Integer.MAX_VALUE);

        // Load and show the current status first
        try {
            Complaint c = complaintService.getComplaintDetails(user, id);
            System.out.println("  Current status: " + c.getStatus());

            // Figure out the only valid next step
            Status current = c.getStatus();
            Status next    = getNextStatus(current);

            if (next == null) {
                System.out.println("  This complaint is already RESOLVED. No further updates possible.");
                return;
            }

            System.out.println("  Next status     : " + next);
            System.out.print("  Confirm update? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("  Update cancelled.");
                return;
            }

            complaintService.updateStatus(user, id, next);
            System.out.println("[OK] Status updated to " + next + ".");

        } catch (UnauthorizedActionException | InvalidComplaintException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (InvalidStatusTransitionException e) {
            // This should not normally happen since we compute the next step,
            // but we catch it defensively
            System.out.println("[!] Invalid transition: " + e.getMessage());
        }
    }

    /**
     * Given the current status, returns the only valid next status.
     * Returns null if already RESOLVED (terminal state).
     */
    private Status getNextStatus(Status current) {
        return switch (current) {
            case REPORTED    -> Status.IN_PROGRESS;
            case IN_PROGRESS -> Status.RESOLVED;
            case RESOLVED    -> null;
        };
    }

    /** Prints counts per status for the staff dashboard. */
    private void doDashboard(User user) {
        System.out.println(SEPARATOR);
        System.out.println("  DASHBOARD - Complaint Summary");
        System.out.println(SEPARATOR);
        try {
            Map<Status, Integer> counts = complaintService.getSummaryCounts(user);
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<Status, Integer> entry : counts.entrySet()) {
                System.out.printf("  %-12s : %d%n", entry.getKey(), entry.getValue());
            }
            System.out.println(SEPARATOR);
            System.out.println("  Total         : " + total);
            System.out.println(SEPARATOR);
        } catch (UnauthorizedActionException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    // ================================================================
    //  Helpers
    // ================================================================

    /** Prints the application banner on startup. */
    private void printBanner() {
        System.out.println();
        System.out.println("============================================");
        System.out.println("   FixIt - Digital Complaint Tracker");
        System.out.println("   KTU OOP Mini-Project  |  Java + MySQL");
        System.out.println("============================================");
        System.out.println();
    }

    /** Prints the start (login / register) menu. */
    private void printStartMenu() {
        System.out.println(SEPARATOR);
        System.out.println("  MAIN MENU");
        System.out.println(SEPARATOR);
        System.out.println("  1. Login");
        System.out.println("  2. Register (Student)");
        System.out.println("  3. Exit");
        System.out.println(SEPARATOR);
        System.out.print("Choice: ");
    }

    /**
     * Reads an integer from the user that is within [min, max].
     * Loops until a valid integer is entered – never crashes on bad input.
     *
     * @param min minimum allowed value (inclusive)
     * @param max maximum allowed value (inclusive)
     * @return the validated integer
     */
    private int readInt(int min, int max) {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                // User typed letters – catch the exception and ask again
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}
