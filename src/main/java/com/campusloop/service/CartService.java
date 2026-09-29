package com.campusloop.service;

import com.campusloop.dao.CartDao;
import com.campusloop.dao.ListingDao;
import com.campusloop.dao.TransactionDao;
import com.campusloop.model.CartItem;
import com.campusloop.model.CartMigrationResult;
import com.campusloop.model.CartView;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingStatus;
import com.campusloop.model.Transaction;
import com.campusloop.util.DbUtil;
import com.campusloop.util.GuestCart;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart rules for guests (cookie) and signed-in students (cart_items).
 *
 * Only AVAILABLE listings can be added, never a student's own listing, and a
 * student's cart never holds the same listing twice. Sold, reserved or removed
 * items already in a cart are shown as unavailable and can't be checked out.
 */
public class CartService {

    public static final int MAX_USER_ITEMS = 50;

    private final CartDao cartDao = new CartDao();
    private final ListingDao listingDao = new ListingDao();
    private final TransactionDao transactionDao = new TransactionDao();

    // ------------------------------------------------------------------ signed-in students

    /** Adds to a student's cart. Returns false if it was already there. */
    public boolean addForUser(int studentId, int listingId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Listing listing = listingDao.findById(conn, listingId);
            requireAddable(listing);
            if (listing.getSellerId() == studentId) {
                throw new ServiceException("This is your own listing, so it can't go in your cart.");
            }
            if (cartDao.contains(conn, studentId, listingId)) {
                return false;
            }
            if (cartDao.count(conn, studentId) >= MAX_USER_ITEMS) {
                throw new ServiceException("Your cart is full. Remove something before adding more.");
            }
            return cartDao.add(conn, studentId, listingId);
        }
    }

    public boolean removeForUser(int studentId, int listingId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return cartDao.remove(conn, studentId, listingId);
        }
    }

    public int countForUser(int studentId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return cartDao.count(conn, studentId);
        }
    }

    /** A student's cart, including each item's in-progress exchange (if any). */
    public CartView viewForUser(int studentId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection()) {
            for (CartDao.Row row : cartDao.listForStudent(conn, studentId)) {
                Listing listing = listingDao.findById(conn, row.getListingId());
                if (listing == null || listing.getStatus() == ListingStatus.REMOVED
                        || listing.getSellerId() == studentId) {
                    cartDao.remove(conn, studentId, row.getListingId());
                    continue;
                }
                CartItem item = new CartItem();
                item.setCartItemId(row.getCartItemId());
                item.setAddedAt(row.getAddedAt());
                item.setListing(listing);
                Transaction active = transactionDao.findActiveForBuyerAndListing(conn, studentId, listing.getId());
                if (active != null) {
                    item.setActiveTransactionId(active.getId());
                    item.setActiveTransactionStatus(active.getStatus());
                }
                items.add(item);
            }
        }
        return new CartView(items, false);
    }

    /**
     * Moves a guest cart into the student's cart after sign-in. Duplicates are
     * merged, the student's own listings and anything no longer available are
     * dropped, and each outcome is counted for a clear message.
     */
    public CartMigrationResult migrateGuestCart(int studentId, List<Integer> guestIds) throws SQLException {
        CartMigrationResult result = new CartMigrationResult();
        if (guestIds == null || guestIds.isEmpty()) {
            return result;
        }
        try (Connection conn = DbUtil.getConnection()) {
            for (Integer id : guestIds) {
                Listing listing = listingDao.findById(conn, id);
                if (listing == null || listing.getStatus() != ListingStatus.AVAILABLE) {
                    result.addUnavailable();
                } else if (listing.getSellerId() == studentId) {
                    result.addOwnItem();
                } else if (cartDao.contains(conn, studentId, id)) {
                    result.addAlreadyInCart();
                } else if (cartDao.count(conn, studentId) >= MAX_USER_ITEMS) {
                    result.addUnavailable();
                } else if (cartDao.add(conn, studentId, id)) {
                    result.addMoved();
                } else {
                    result.addAlreadyInCart();
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ guests (cookie)

    /** Returns the new guest id list after adding {@code listingId}. */
    public List<Integer> addForGuest(List<Integer> current, int listingId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            requireAddable(listingDao.findById(conn, listingId));
        }
        List<Integer> ids = new ArrayList<>(current);
        if (ids.contains(listingId)) {
            return ids;
        }
        if (ids.size() >= GuestCart.MAX_ITEMS) {
            throw new ServiceException("Your cart is full (" + GuestCart.MAX_ITEMS
                    + " items). Sign in or remove something before adding more.");
        }
        ids.add(listingId);
        return ids;
    }

    /**
     * Builds the guest cart view. Ids that no longer exist (or were removed by
     * their seller) are dropped; the caller rewrites the cookie with {@link CartView}
     * ids so the cookie heals itself.
     */
    public CartView viewForGuest(List<Integer> ids) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection()) {
            for (int i = ids.size() - 1; i >= 0; i--) {
                Listing listing = listingDao.findById(conn, ids.get(i));
                if (listing == null || listing.getStatus() == ListingStatus.REMOVED) {
                    continue;
                }
                CartItem item = new CartItem();
                item.setListing(listing);
                items.add(item);
            }
        }
        return new CartView(items, true);
    }

    // ------------------------------------------------------------------ helpers

    private static void requireAddable(Listing listing) throws ServiceException {
        if (listing == null || listing.getStatus() == ListingStatus.REMOVED) {
            throw new NotFoundException("This item doesn't exist or has been removed by its seller.");
        }
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new ServiceException("Sorry, this item has already been sold.");
        }
        if (listing.getStatus() == ListingStatus.RESERVED) {
            throw new ServiceException("This item is reserved for another student's confirmed exchange.");
        }
    }
}
