package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.util.List;

/** Cart page model: lines plus totals computed only from items the student can still get. */
public class CartView {

    private final List<CartItem> items;
    private final boolean guest;

    public CartView(List<CartItem> items, boolean guest) {
        this.items = items;
        this.guest = guest;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isGuest() {
        return guest;
    }

    public int getCount() {
        return items.size();
    }

    /** Items that are still obtainable (available, or already in an exchange with this student). */
    public int getActiveCount() {
        int n = 0;
        for (CartItem i : items) {
            if (!i.isUnavailable()) {
                n++;
            }
        }
        return n;
    }

    public int getUnavailableCount() {
        return items.size() - getActiveCount();
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem i : items) {
            if (!i.isUnavailable()) {
                total = total.add(i.getListing().getPrice());
            }
        }
        return total;
    }

    public String getTotalLabel() {
        return Format.inr(getTotal());
    }
}
