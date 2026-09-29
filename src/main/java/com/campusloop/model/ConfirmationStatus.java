package com.campusloop.model;

/** Mirrors transactions.seller_confirmation_status. */
public enum ConfirmationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    REJECTED("Rejected");

    private final String label;

    ConfirmationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return name();
    }
}
