package com.campusloop.model;

import java.time.LocalDateTime;

/**
 * One line of a cart. For guests (cookie cart) cartItemId is 0 and addedAt is null.
 * activeTransaction is the viewer's in-progress exchange request for this listing, if any.
 */
public class CartItem {

    private int cartItemId;
    private Listing listing;
    private LocalDateTime addedAt;
    private Integer activeTransactionId;
    private TransactionStatus activeTransactionStatus;

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Integer getActiveTransactionId() {
        return activeTransactionId;
    }

    public void setActiveTransactionId(Integer activeTransactionId) {
        this.activeTransactionId = activeTransactionId;
    }

    public TransactionStatus getActiveTransactionStatus() {
        return activeTransactionStatus;
    }

    public void setActiveTransactionStatus(TransactionStatus activeTransactionStatus) {
        this.activeTransactionStatus = activeTransactionStatus;
    }

    public boolean isHasActiveRequest() {
        return activeTransactionId != null;
    }

    /** Checkout is possible only for AVAILABLE items with no request in progress. */
    public boolean isCheckoutReady() {
        return listing != null && listing.isAvailable() && activeTransactionId == null;
    }

    /** Sold/removed/reserved-for-someone-else items the buyer can no longer get. */
    public boolean isUnavailable() {
        return listing == null || (!listing.isAvailable() && activeTransactionId == null);
    }
}
