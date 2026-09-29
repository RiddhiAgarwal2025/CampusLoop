package com.campusloop.model;

import java.util.List;

/** Profile page model for the signed-in student (private: includes email and wallet). */
public class ProfileView {

    private final Student student;
    private final SellerRating rating;
    private final int completedSales;
    private final int completedPurchases;
    private final int activeListings;
    private final List<SellerReview> reviews;

    public ProfileView(Student student, SellerRating rating, int completedSales, int completedPurchases,
                       int activeListings, List<SellerReview> reviews) {
        this.student = student;
        this.rating = rating;
        this.completedSales = completedSales;
        this.completedPurchases = completedPurchases;
        this.activeListings = activeListings;
        this.reviews = reviews;
    }

    public Student getStudent() {
        return student;
    }

    public SellerRating getRating() {
        return rating;
    }

    public int getCompletedSales() {
        return completedSales;
    }

    public int getCompletedPurchases() {
        return completedPurchases;
    }

    public int getActiveListings() {
        return activeListings;
    }

    public List<SellerReview> getReviews() {
        return reviews;
    }
}
