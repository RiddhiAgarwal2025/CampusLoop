package com.campusloop.model;

/** Mirrors listings.item_condition. */
public enum ItemCondition {
    NEW("New", "Unused, possibly in original packaging."),
    LIKE_NEW("Like new", "Used briefly, no visible wear."),
    GOOD("Good", "Normal signs of use, fully functional."),
    FAIR("Fair", "Noticeable wear, still works as intended.");

    private final String label;
    private final String hint;

    ItemCondition(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }

    public String getLabel() {
        return label;
    }

    public String getHint() {
        return hint;
    }

    public String getCode() {
        return name();
    }

    /** Parses a submitted value; returns null for anything not in the enum. */
    public static ItemCondition parse(String value) {
        if (value == null) {
            return null;
        }
        for (ItemCondition c : values()) {
            if (c.name().equals(value.trim())) {
                return c;
            }
        }
        return null;
    }
}
