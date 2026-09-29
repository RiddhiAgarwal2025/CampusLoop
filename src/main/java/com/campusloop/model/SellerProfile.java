package com.campusloop.model;

import com.campusloop.util.Format;

import java.time.LocalDateTime;
import java.util.List;

/** Public seller page model. Deliberately excludes email, wallet and other private data. */
public class SellerProfile {

    private final int id;
    private final String name;
    private final String profileImage;
    private final LocalDateTime memberSince;
    private final SellerRating rating;
    private final int completedSales;
    private final List<Listing> listings;
    private final List<SellerReview> reviews;

    public SellerProfile(int id, String name, String profileImage, LocalDateTime memberSince,
                         SellerRating rating, int completedSales, List<Listing> listings,
                         List<SellerReview> reviews) {
        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
        this.memberSince = memberSince;
        this.rating = rating;
        this.completedSales = completedSales;
        this.listings = listings;
        this.reviews = reviews;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFirstName() {
        return Format.firstName(name);
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getMemberSinceLabel() {
        return Format.monthYear(memberSince);
    }

    public SellerRating getRating() {
        return rating;
    }

    public int getCompletedSales() {
        return completedSales;
    }

    public List<Listing> getListings() {
        return listings;
    }

    public List<SellerReview> getReviews() {
        return reviews;
    }
}
