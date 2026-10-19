package com.fixit.model;

/**
 * The four complaint categories supported by FixIt.
 * Each value carries a human-readable display name.
 *
 * OOP concept: Enum with a field and a static helper method.
 */
public enum ComplaintType {

    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    WIFI("Wi-Fi"),
    GENERIC("Generic");

    /** A friendly label shown in menus and reports. */
    private final String displayName;

    ComplaintType(String displayName) {
        this.displayName = displayName;
    }

    /** @return the human-readable name for this type */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a menu choice (1-4) to a ComplaintType.
     *
     * @param choice integer typed by the user (1 = ELECTRICAL … 4 = GENERIC)
     * @return the matching ComplaintType, or null if the choice is out of range
     */
    public static ComplaintType fromMenuChoice(int choice) {
        return switch (choice) {
            case 1 -> ELECTRICAL;
            case 2 -> PLUMBING;
            case 3 -> WIFI;
            case 4 -> GENERIC;
            default -> null;   // caller must handle the null case
        };
    }
}
