package com.campusloop.model;

/**
 * Raw values submitted by the Sell / Edit form, kept as text so an invalid
 * submission can be shown back to the student exactly as typed.
 */
public class ListingForm {

    private String title = "";
    private String description = "";
    private String categoryId = "";
    private String price = "";
    private String condition = "";

    public static ListingForm from(Listing l) {
        ListingForm f = new ListingForm();
        f.setTitle(l.getTitle());
        f.setDescription(l.getDescription());
        f.setCategoryId(String.valueOf(l.getCategoryId()));
        f.setPrice(l.getPrice() == null ? "" : l.getPrice().stripTrailingZeros().toPlainString());
        f.setCondition(l.getCondition() == null ? "" : l.getCondition().name());
        return f;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId == null ? "" : categoryId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price == null ? "" : price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition == null ? "" : condition;
    }
}
