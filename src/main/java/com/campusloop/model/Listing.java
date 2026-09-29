package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row of the listings table plus joined display data
 * (seller name/photo, category, seller rating computed from seller_reviews).
 */
public class Listing {

    private int id;
    private int sellerId;
    private int categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private ItemCondition condition;
    private String imageUrl;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String sellerName;
    private String sellerImage;
    private String categoryName;
    private String categorySlug;
    private double sellerRating;
    private int sellerReviewCount;

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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public double getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(double sellerRating) {
        this.sellerRating = sellerRating;
    }

    public int getSellerReviewCount() {
        return sellerReviewCount;
    }

    public void setSellerReviewCount(int sellerReviewCount) {
        this.sellerReviewCount = sellerReviewCount;
    }

    // ---------- display helpers ----------

    public String getPriceLabel() {
        return Format.inr(price);
    }

    public String getConditionLabel() {
        return condition == null ? "" : condition.getLabel();
    }

    public String getStatusLabel() {
        return status == null ? "" : status.getLabel();
    }

    public boolean isAvailable() {
        return status == ListingStatus.AVAILABLE;
    }

    /** Only AVAILABLE listings may be edited or removed by their seller. */
    public boolean isEditable() {
        return status == ListingStatus.AVAILABLE;
    }

    public String getSellerFirstName() {
        return Format.firstName(sellerName);
    }

    public String getCreatedAtLabel() {
        return Format.date(createdAt == null ? null : createdAt.toLocalDate());
    }
}
