package com.campusloop.model;

import java.util.List;

/** Everything the home page shows; all numbers are live database counts. */
public class HomeSummary {

    private final List<Listing> trending;
    private final List<Category> categories;
    private final int itemsRecirculated;
    private final int availableListings;
    private final int students;

    public HomeSummary(List<Listing> trending, List<Category> categories, int itemsRecirculated,
                       int availableListings, int students) {
        this.trending = trending;
        this.categories = categories;
        this.itemsRecirculated = itemsRecirculated;
        this.availableListings = availableListings;
        this.students = students;
    }

    public List<Listing> getTrending() {
        return trending;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public int getItemsRecirculated() {
        return itemsRecirculated;
    }

    public int getAvailableListings() {
        return availableListings;
    }

    public int getStudents() {
        return students;
    }
}
