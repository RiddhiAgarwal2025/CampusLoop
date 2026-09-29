package com.campusloop.model;

/** Mirrors listings.status. */
public enum ListingStatus {
    AVAILABLE("Available", "success"),
    RESERVED("Reserved", "warning"),
    SOLD("Sold", "neutral"),
    REMOVED("Removed", "muted");

    private final String label;
    private final String tone;

    ListingStatus(String label, String tone) {
        this.label = label;
        this.tone = tone;
    }

    public String getLabel() {
        return label;
    }

    /** CSS badge modifier used by the statusBadge tag. */
    public String getTone() {
        return tone;
    }

    public String getCode() {
        return name();
    }
}
