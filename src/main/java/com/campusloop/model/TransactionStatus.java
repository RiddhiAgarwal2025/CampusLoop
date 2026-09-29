package com.campusloop.model;

/** Mirrors transactions.transaction_status. */
public enum TransactionStatus {
    AWAITING_SELLER("Awaiting seller", "warning"),
    AWAITING_PAYMENT("Awaiting payment", "info"),
    COMPLETED("Completed", "success"),
    REJECTED("Rejected", "danger"),
    CANCELLED("Cancelled", "muted");

    private final String label;
    private final String tone;

    TransactionStatus(String label, String tone) {
        this.label = label;
        this.tone = tone;
    }

    public String getLabel() {
        return label;
    }

    public String getTone() {
        return tone;
    }

    public String getCode() {
        return name();
    }

    /** Request still in progress (buyer or seller can act on it). */
    public boolean isActive() {
        return this == AWAITING_SELLER || this == AWAITING_PAYMENT;
    }
}
