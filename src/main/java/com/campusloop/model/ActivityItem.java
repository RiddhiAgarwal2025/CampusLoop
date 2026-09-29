package com.campusloop.model;

import com.campusloop.util.Format;

import java.time.LocalDateTime;

/** One entry of the dashboard "Recent activity" feed, built from real database rows. */
public class ActivityItem {

    /** Icon/kind keys used by the dashboard: topup, purchase, sale, request, listing, review. */
    private final String kind;
    private final String title;
    private final String detail;
    private final String amountLabel;
    private final boolean credit;
    private final LocalDateTime when;
    /** Internal application path, e.g. "/exchange?id=4". */
    private final String link;

    public ActivityItem(String kind, String title, String detail, String amountLabel,
                        boolean credit, LocalDateTime when, String link) {
        this.kind = kind;
        this.title = title;
        this.detail = detail;
        this.amountLabel = amountLabel;
        this.credit = credit;
        this.when = when;
        this.link = link;
    }

    public String getKind() {
        return kind;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getAmountLabel() {
        return amountLabel;
    }

    public boolean isCredit() {
        return credit;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public String getLink() {
        return link;
    }

    public String getWhenLabel() {
        return Format.relative(when);
    }
}
