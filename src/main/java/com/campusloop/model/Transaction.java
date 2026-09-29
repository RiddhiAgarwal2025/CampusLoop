package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Row of the transactions table: an exchange request that becomes a completed
 * purchase once the seller confirms and the buyer pays. Includes joined data
 * for display (listing, buyer and seller names, whether a review exists).
 */
public class Transaction {

    private int id;
    private int buyerId;
    private int sellerId;
    private int listingId;
    private BigDecimal amount;
    private String exchangeLocation;
    private LocalDate exchangeDate;
    private LocalTime exchangeTime;
    private String exchangeNote;
    private ConfirmationStatus confirmationStatus;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    private String listingTitle;
    private String listingImage;
    private ListingStatus listingStatus;
    private String buyerName;
    private String buyerImage;
    private String sellerName;
    private String sellerImage;
    private boolean reviewed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getExchangeLocation() {
        return exchangeLocation;
    }

    public void setExchangeLocation(String exchangeLocation) {
        this.exchangeLocation = exchangeLocation;
    }

    public LocalDate getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(LocalDate exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    public LocalTime getExchangeTime() {
        return exchangeTime;
    }

    public void setExchangeTime(LocalTime exchangeTime) {
        this.exchangeTime = exchangeTime;
    }

    public String getExchangeNote() {
        return exchangeNote;
    }

    public void setExchangeNote(String exchangeNote) {
        this.exchangeNote = exchangeNote;
    }

    public ConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(ConfirmationStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public String getListingImage() {
        return listingImage;
    }

    public void setListingImage(String listingImage) {
        this.listingImage = listingImage;
    }

    public ListingStatus getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(ListingStatus listingStatus) {
        this.listingStatus = listingStatus;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerImage() {
        return buyerImage;
    }

    public void setBuyerImage(String buyerImage) {
        this.buyerImage = buyerImage;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerImage() {
        return sellerImage;
    }

    public void setSellerImage(String sellerImage) {
        this.sellerImage = sellerImage;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    // ---------- state helpers used by services and JSPs ----------

    public boolean isAwaitingSeller() {
        return status == TransactionStatus.AWAITING_SELLER;
    }

    public boolean isAwaitingPayment() {
        return status == TransactionStatus.AWAITING_PAYMENT;
    }

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isActive() {
        return status != null && status.isActive();
    }

    /** Buyer may review only a completed transaction that has no review yet. */
    public boolean isReviewable() {
        return isCompleted() && !reviewed;
    }

    // ---------- display helpers ----------

    public String getAmountLabel() {
        return Format.inr(amount);
    }

    public String getExchangeDateLabel() {
        return Format.date(exchangeDate);
    }

    public String getExchangeLongDateLabel() {
        return Format.longDate(exchangeDate);
    }

    public String getExchangeTimeLabel() {
        return Format.time(exchangeTime);
    }

    public String getTransactionDateLabel() {
        return Format.dateTime(transactionDate);
    }

    public String getConfirmedAtLabel() {
        return Format.dateTime(confirmedAt);
    }

    public String getCompletedAtLabel() {
        return Format.dateTime(completedAt);
    }

    public String getStatusLabel() {
        return status == null ? "" : status.getLabel();
    }

    public String getStatusTone() {
        return status == null ? "muted" : status.getTone();
    }

    public String getBuyerFirstName() {
        return Format.firstName(buyerName);
    }

    public String getSellerFirstName() {
        return Format.firstName(sellerName);
    }
}
