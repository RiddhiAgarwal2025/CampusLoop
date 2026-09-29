package com.campusloop.model;

import java.util.List;

/** Product detail page model. */
public class ProductView {

    private final Listing listing;
    private final SellerRating sellerRating;
    private final List<SellerReview> sellerReviews;
    private final List<Listing> moreFromSeller;
    private final boolean owner;

    public ProductView(Listing listing, SellerRating sellerRating, List<SellerReview> sellerReviews,
                       List<Listing> moreFromSeller, boolean owner) {
        this.listing = listing;
        this.sellerRating = sellerRating;
        this.sellerReviews = sellerReviews;
        this.moreFromSeller = moreFromSeller;
        this.owner = owner;
    }

    public Listing getListing() {
        return listing;
    }

    public SellerRating getSellerRating() {
        return sellerRating;
    }

    public List<SellerReview> getSellerReviews() {
        return sellerReviews;
    }

    public List<Listing> getMoreFromSeller() {
        return moreFromSeller;
    }

    /** True when the signed-in student is this listing's seller. */
    public boolean isOwner() {
        return owner;
    }
}
