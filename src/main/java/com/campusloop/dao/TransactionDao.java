package com.campusloop.dao;

import com.campusloop.model.ConfirmationStatus;
import com.campusloop.model.ListingStatus;
import com.campusloop.model.Transaction;
import com.campusloop.model.TransactionStatus;
import com.campusloop.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC access to the transactions table: one row per exchange request, which
 * becomes the completed purchase record once the seller confirms and the buyer pays.
 *
 * Status changes are written with guarded UPDATEs (the expected current status is
 * part of the WHERE clause), so a stale or replayed request can never move a
 * transaction backwards.
 */
public class TransactionDao {

    private static final String BASIC_COLUMNS =
            "t.transaction_id, t.buyer_id, t.seller_id, t.listing_id, t.amount, t.exchange_location, "
            + "t.exchange_date, t.exchange_time, t.exchange_note, t.seller_confirmation_status, "
            + "t.transaction_status, t.transaction_date, t.confirmed_at, t.completed_at ";

    private static final String SELECT_FULL =
            "SELECT " + BASIC_COLUMNS + ", "
            + "l.title AS listing_title, l.image_url AS listing_image, l.status AS listing_status, "
            + "b.name AS buyer_name, b.profile_image AS buyer_image, "
            + "s.name AS seller_name, s.profile_image AS seller_image, "
            + "EXISTS (SELECT 1 FROM seller_reviews r WHERE r.transaction_id = t.transaction_id) AS reviewed "
            + "FROM transactions t "
            + "JOIN listings l ON l.listing_id = t.listing_id "
            + "JOIN students b ON b.student_id = t.buyer_id "
            + "JOIN students s ON s.student_id = t.seller_id ";

    // ------------------------------------------------------------------ create

    public int insert(Connection conn, int buyerId, int sellerId, int listingId, BigDecimal amount,
                      String location, LocalDate date, LocalTime time, String note) throws SQLException {
        String sql = "INSERT INTO transactions (buyer_id, seller_id, listing_id, amount, exchange_location, "
                + "exchange_date, exchange_time, exchange_note, seller_confirmation_status, transaction_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 'AWAITING_SELLER')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, sellerId);
            ps.setInt(3, listingId);
            ps.setBigDecimal(4, amount);
            ps.setString(5, location);
            ps.setObject(6, date);
            ps.setObject(7, time);
            ps.setString(8, note);
            ps.executeUpdate();
            return DbUtil.generatedId(ps);
        }
    }

    // ------------------------------------------------------------------ reads

    public Transaction findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "WHERE t.transaction_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapFull(rs) : null;
            }
        }
    }

    /** Locks a transaction row (SELECT ... FOR UPDATE) inside the caller's DB transaction. */
    public Transaction lockById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + BASIC_COLUMNS + "FROM transactions t WHERE t.transaction_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBasic(rs) : null;
            }
        }
    }

    /** The buyer's in-progress (AWAITING_SELLER / AWAITING_PAYMENT) request for a listing, if any. */
    public Transaction findActiveForBuyerAndListing(Connection conn, int buyerId, int listingId) throws SQLException {
        String sql = "SELECT " + BASIC_COLUMNS + "FROM transactions t "
                + "WHERE t.buyer_id = ? AND t.listing_id = ? "
                + "AND t.transaction_status IN ('AWAITING_SELLER', 'AWAITING_PAYMENT') "
                + "ORDER BY t.transaction_id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBasic(rs) : null;
            }
        }
    }

    /** Everything this student bought or requested, newest first. */
    public List<Transaction> listForBuyer(Connection conn, int buyerId, int limit) throws SQLException {
        return list(conn, SELECT_FULL + "WHERE t.buyer_id = ? "
                + "ORDER BY COALESCE(t.completed_at, t.confirmed_at, t.transaction_date) DESC, t.transaction_id DESC "
                + "LIMIT ?", buyerId, limit);
    }

    /** Everything requested from / sold by this student, newest first. */
    public List<Transaction> listForSeller(Connection conn, int sellerId, int limit) throws SQLException {
        return list(conn, SELECT_FULL + "WHERE t.seller_id = ? "
                + "ORDER BY COALESCE(t.completed_at, t.confirmed_at, t.transaction_date) DESC, t.transaction_id DESC "
                + "LIMIT ?", sellerId, limit);
    }

    /** listing_id -> number of requests waiting for this seller's confirmation. */
    public Map<Integer, Integer> awaitingSellerCountsByListing(Connection conn, int sellerId) throws SQLException {
        String sql = "SELECT listing_id, COUNT(*) AS n FROM transactions "
                + "WHERE seller_id = ? AND transaction_status = 'AWAITING_SELLER' GROUP BY listing_id";
        Map<Integer, Integer> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getInt("listing_id"), rs.getInt("n"));
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ guarded status changes

    /** AWAITING_SELLER -> AWAITING_PAYMENT (seller confirmed). Returns rows changed. */
    public int markConfirmed(Connection conn, int id) throws SQLException {
        return update(conn, "UPDATE transactions SET seller_confirmation_status = 'CONFIRMED', "
                + "transaction_status = 'AWAITING_PAYMENT', confirmed_at = NOW() "
                + "WHERE transaction_id = ? AND transaction_status = 'AWAITING_SELLER'", id);
    }

    /** AWAITING_SELLER or AWAITING_PAYMENT -> REJECTED (seller declined or released). */
    public int markRejected(Connection conn, int id) throws SQLException {
        return update(conn, "UPDATE transactions SET seller_confirmation_status = 'REJECTED', "
                + "transaction_status = 'REJECTED' "
                + "WHERE transaction_id = ? AND transaction_status IN ('AWAITING_SELLER', 'AWAITING_PAYMENT')", id);
    }

    /** AWAITING_SELLER or AWAITING_PAYMENT -> CANCELLED (buyer cancelled). */
    public int markCancelled(Connection conn, int id) throws SQLException {
        return update(conn, "UPDATE transactions SET transaction_status = 'CANCELLED' "
                + "WHERE transaction_id = ? AND transaction_status IN ('AWAITING_SELLER', 'AWAITING_PAYMENT')", id);
    }

    /** AWAITING_PAYMENT (with seller confirmation) -> COMPLETED (buyer paid). */
    public int markCompleted(Connection conn, int id) throws SQLException {
        return update(conn, "UPDATE transactions SET transaction_status = 'COMPLETED', completed_at = NOW() "
                + "WHERE transaction_id = ? AND transaction_status = 'AWAITING_PAYMENT' "
                + "AND seller_confirmation_status = 'CONFIRMED'", id);
    }

    /**
     * Cancels every other in-progress request for a listing (used when the seller
     * confirms one buyer, and after a sale). Pass 0 to cancel all of them.
     */
    public int cancelOtherActiveRequests(Connection conn, int listingId, int keepTransactionId) throws SQLException {
        String sql = "UPDATE transactions SET transaction_status = 'CANCELLED' "
                + "WHERE listing_id = ? AND transaction_id <> ? "
                + "AND transaction_status IN ('AWAITING_SELLER', 'AWAITING_PAYMENT')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, keepTransactionId);
            return ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ helpers

    private int update(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private List<Transaction> list(Connection conn, String sql, int studentId, int limit) throws SQLException {
        List<Transaction> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapFull(rs));
                }
            }
        }
        return out;
    }

    private Transaction mapBasic(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("transaction_id"));
        t.setBuyerId(rs.getInt("buyer_id"));
        t.setSellerId(rs.getInt("seller_id"));
        t.setListingId(rs.getInt("listing_id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setExchangeLocation(rs.getString("exchange_location"));
        t.setExchangeDate(DbUtil.getDate(rs, "exchange_date"));
        t.setExchangeTime(DbUtil.getTime(rs, "exchange_time"));
        t.setExchangeNote(rs.getString("exchange_note"));
        t.setConfirmationStatus(ConfirmationStatus.valueOf(rs.getString("seller_confirmation_status")));
        t.setStatus(TransactionStatus.valueOf(rs.getString("transaction_status")));
        t.setTransactionDate(DbUtil.getDateTime(rs, "transaction_date"));
        t.setConfirmedAt(DbUtil.getDateTime(rs, "confirmed_at"));
        t.setCompletedAt(DbUtil.getDateTime(rs, "completed_at"));
        return t;
    }

    private Transaction mapFull(ResultSet rs) throws SQLException {
        Transaction t = mapBasic(rs);
        t.setListingTitle(rs.getString("listing_title"));
        t.setListingImage(rs.getString("listing_image"));
        t.setListingStatus(ListingStatus.valueOf(rs.getString("listing_status")));
        t.setBuyerName(rs.getString("buyer_name"));
        t.setBuyerImage(rs.getString("buyer_image"));
        t.setSellerName(rs.getString("seller_name"));
        t.setSellerImage(rs.getString("seller_image"));
        t.setReviewed(rs.getBoolean("reviewed"));
        return t;
    }
}
