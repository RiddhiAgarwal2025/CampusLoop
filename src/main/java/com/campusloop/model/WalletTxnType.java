package com.campusloop.model;

/** Mirrors wallet_transactions.txn_type. */
public enum WalletTxnType {
    TOPUP("Wallet top-up"),
    PURCHASE("Purchase"),
    SALE_CREDIT("Sale credit");

    private final String label;

    WalletTxnType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return name();
    }
}
