package com.campusloop.model;

import com.campusloop.util.Format;

/** Average rating and review count for one seller, computed from seller_reviews. */
public class SellerRating {

    private final int sellerId;
    private final double average;
    private final int count;

    public SellerRating(int sellerId, double average, int count) {
        this.sellerId = sellerId;
        this.average = average;
        this.count = count;
    }

    public int getSellerId() {
        return sellerId;
    }

    public double getAverage() {
        return average;
    }

    public int getCount() {
        return count;
    }

    public boolean isRated() {
        return count > 0;
    }

    public String getAverageLabel() {
        return Format.rating(average);
    }

    /** "12 reviews" / "1 review" / "No reviews yet". */
    public String getCountLabel() {
        if (count == 0) {
            return "No reviews yet";
        }
        return count == 1 ? "1 review" : count + " reviews";
    }
}
