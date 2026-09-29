package com.campusloop.model;

import java.util.List;

/** Dashboard page model. Every number comes from a live database query. */
public class DashboardView {

    private final Student student;
    private final DashboardStats stats;
    private final List<ActivityItem> activity;
    private final String greeting;
    private final String dateLabel;
    private final int itemsRecirculated;
    private final int pointsPerResale;

    public DashboardView(Student student, DashboardStats stats, List<ActivityItem> activity, String greeting,
                         String dateLabel, int itemsRecirculated, int pointsPerResale) {
        this.student = student;
        this.stats = stats;
        this.activity = activity;
        this.greeting = greeting;
        this.dateLabel = dateLabel;
        this.itemsRecirculated = itemsRecirculated;
        this.pointsPerResale = pointsPerResale;
    }

    public Student getStudent() {
        return student;
    }

    public DashboardStats getStats() {
        return stats;
    }

    public List<ActivityItem> getActivity() {
        return activity;
    }

    /** "Good morning" / "Good afternoon" / "Good evening" in the campus time zone. */
    public String getGreeting() {
        return greeting;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    /** Campus-wide completed resales. */
    public int getItemsRecirculated() {
        return itemsRecirculated;
    }

    public int getPointsPerResale() {
        return pointsPerResale;
    }
}
