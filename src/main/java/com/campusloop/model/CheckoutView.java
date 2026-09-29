package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.util.List;

/** Checkout (exchange details) page model. */
public class CheckoutView {

    private final Listing listing;
    private final SellerRating sellerRating;
    private final BigDecimal walletBalance;
    private final String minDate;
    private final String maxDate;
    private final String earliestTime;
    private final String latestTime;
    private final List<String> suggestedLocations;

    public CheckoutView(Listing listing, SellerRating sellerRating, BigDecimal walletBalance, String minDate,
                        String maxDate, String earliestTime, String latestTime, List<String> suggestedLocations) {
        this.listing = listing;
        this.sellerRating = sellerRating;
        this.walletBalance = walletBalance;
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.earliestTime = earliestTime;
        this.latestTime = latestTime;
        this.suggestedLocations = suggestedLocations;
    }

    public Listing getListing() {
        return listing;
    }

    public SellerRating getSellerRating() {
        return sellerRating;
    }

    public String getWalletLabel() {
        return Format.inr(walletBalance);
    }

    /** True when the current balance already covers the price. */
    public boolean isCanAfford() {
        return walletBalance.compareTo(listing.getPrice()) >= 0;
    }

    public String getShortfallLabel() {
        return Format.inr(listing.getPrice().subtract(walletBalance).max(BigDecimal.ZERO));
    }

    public String getMinDate() {
        return minDate;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public String getEarliestTime() {
        return earliestTime;
    }

    public String getLatestTime() {
        return latestTime;
    }

    public List<String> getSuggestedLocations() {
        return suggestedLocations;
    }
}
