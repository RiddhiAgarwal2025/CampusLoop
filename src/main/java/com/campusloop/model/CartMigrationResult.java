package com.campusloop.model;

/** Outcome of moving a guest (cookie) cart into a signed-in student's cart. */
public class CartMigrationResult {

    private int moved;
    private int alreadyInCart;
    private int ownItems;
    private int unavailable;

    public void addMoved() {
        moved++;
    }

    public void addAlreadyInCart() {
        alreadyInCart++;
    }

    public void addOwnItem() {
        ownItems++;
    }

    public void addUnavailable() {
        unavailable++;
    }

    /** Human summary for the post-login flash message, e.g. "2 items from your guest cart were added." */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (moved > 0) {
            sb.append(moved == 1 ? "1 item" : moved + " items").append(" from your guest cart ")
              .append(moved == 1 ? "was" : "were").append(" added to your cart.");
        }
        if (alreadyInCart > 0) {
            append(sb, (alreadyInCart == 1 ? "1 item was" : alreadyInCart + " items were") + " already in your cart.");
        }
        if (ownItems > 0) {
            append(sb, (ownItems == 1 ? "1 item is" : ownItems + " items are")
                    + " your own listing, so " + (ownItems == 1 ? "it was" : "they were") + " left out.");
        }
        if (unavailable > 0) {
            append(sb, (unavailable == 1 ? "1 item is" : unavailable + " items are")
                    + " no longer available and " + (unavailable == 1 ? "was" : "were") + " removed.");
        }
        return sb.toString();
    }

    private static void append(StringBuilder sb, String text) {
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(text);
    }
}
