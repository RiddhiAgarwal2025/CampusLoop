package com.campusloop.dao;

import com.campusloop.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** JDBC access to cart_items (authenticated carts). Guest carts live in the cl_guest_cart cookie. */
public class CartDao {

    /** A cart row: which listing, when it was added. */
    public static final class Row {
        private final int cartItemId;
        private final int listingId;
        private final LocalDateTime addedAt;

        Row(int cartItemId, int listingId, LocalDateTime addedAt) {
            this.cartItemId = cartItemId;
            this.listingId = listingId;
            this.addedAt = addedAt;
        }

        public int getCartItemId() {
            return cartItemId;
        }

        public int getListingId() {
            return listingId;
        }

        public LocalDateTime getAddedAt() {
            return addedAt;
        }
    }

    public List<Row> listForStudent(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT cart_item_id, listing_id, added_at FROM cart_items WHERE student_id = ? "
                + "ORDER BY added_at DESC, cart_item_id DESC";
        List<Row> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Row(rs.getInt("cart_item_id"), rs.getInt("listing_id"),
                            DbUtil.getDateTime(rs, "added_at")));
                }
            }
        }
        return rows;
    }

    /** Adds a listing; returns false if it was already in the cart (UNIQUE key, no error). */
    public boolean add(Connection conn, int studentId, int listingId) throws SQLException {
        String sql = "INSERT IGNORE INTO cart_items (student_id, listing_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, listingId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Removes one listing from this student's cart only. */
    public boolean remove(Connection conn, int studentId, int listingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM cart_items WHERE student_id = ? AND listing_id = ?")) {
            ps.setInt(1, studentId);
            ps.setInt(2, listingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean contains(Connection conn, int studentId, int listingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM cart_items WHERE student_id = ? AND listing_id = ?")) {
            ps.setInt(1, studentId);
            ps.setInt(2, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int count(Connection conn, int studentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM cart_items WHERE student_id = ?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Removes a listing from every student's cart (after it is sold or removed). */
    public int deleteByListing(Connection conn, int listingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE listing_id = ?")) {
            ps.setInt(1, listingId);
            return ps.executeUpdate();
        }
    }
}
