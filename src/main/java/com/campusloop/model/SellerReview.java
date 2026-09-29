package com.campusloop.model;

import com.campusloop.util.Format;

import java.time.LocalDateTime;

/** Row of the seller_reviews table plus reviewer and listing names for display. */
public class SellerReview {

    private int id;
    private int sellerId;
    private int reviewerId;
    private int transactionId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    private String reviewerName;
    private String reviewerImage;
    private String listingTitle;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerImage() {
        return reviewerImage;
    }

    public void setReviewerImage(String reviewerImage) {
        this.reviewerImage = reviewerImage;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public String getReviewerFirstName() {
        return Format.firstName(reviewerName);
    }

    public String getCreatedAtLabel() {
        return Format.date(createdAt == null ? null : createdAt.toLocalDate());
    }
}
