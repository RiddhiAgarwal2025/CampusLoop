package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;

/** Numbers shown on the dashboard. Every value comes from a database query. */
public class DashboardStats {

    private BigDecimal walletBalance = BigDecimal.ZERO;
    private int activeListings;
    private int soldListings;
    private int completedPurchases;
    private int completedSales;
    private int pendingConfirmations;
    private int awaitingPayment;
    private int impactPoints;
    private int completedResales;

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public int getActiveListings() {
        return activeListings;
    }

    public void setActiveListings(int activeListings) {
        this.activeListings = activeListings;
    }

    public int getSoldListings() {
        return soldListings;
    }

    public void setSoldListings(int soldListings) {
        this.soldListings = soldListings;
    }

    public int getCompletedPurchases() {
        return completedPurchases;
    }

    public void setCompletedPurchases(int completedPurchases) {
        this.completedPurchases = completedPurchases;
    }

    public int getCompletedSales() {
        return completedSales;
    }

    public void setCompletedSales(int completedSales) {
        this.completedSales = completedSales;
    }

    /** Requests on my listings waiting for MY confirmation (as seller). */
    public int getPendingConfirmations() {
        return pendingConfirmations;
    }

    public void setPendingConfirmations(int pendingConfirmations) {
        this.pendingConfirmations = pendingConfirmations;
    }

    /** My requests confirmed by the seller and waiting for MY payment (as buyer). */
    public int getAwaitingPayment() {
        return awaitingPayment;
    }

    public void setAwaitingPayment(int awaitingPayment) {
        this.awaitingPayment = awaitingPayment;
    }

    public int getImpactPoints() {
        return impactPoints;
    }

    public void setImpactPoints(int impactPoints) {
        this.impactPoints = impactPoints;
    }

    /** Completed resales this student took part in (as buyer or seller). */
    public int getCompletedResales() {
        return completedResales;
    }

    public void setCompletedResales(int completedResales) {
        this.completedResales = completedResales;
    }

    public String getWalletLabel() {
        return Format.inr(walletBalance);
    }
}
