package com.campusloop.dao;

import com.campusloop.model.SellerRating;
import com.campusloop.model.SellerReview;
import com.campusloop.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC access to seller_reviews. Ratings are always aggregated from real rows. */
public class ReviewDao {

    public SellerRating ratingForSeller(Connection conn, int sellerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS review_count, COALESCE(ROUND(AVG(rating), 1), 0) AS avg_rating "
                + "FROM seller_reviews WHERE seller_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new SellerRating(sellerId, rs.getDouble("avg_rating"), rs.getInt("review_count"));
            }
        }
    }

    /** Most recent reviews of a seller, with reviewer name/photo and the item that was bought. */
    public List<SellerReview> listForSeller(Connection conn, int sellerId, int limit) throws SQLException {
        String sql = "SELECT r.review_id, r.seller_id, r.reviewer_id, r.transaction_id, r.rating, r.comment, "
                + "r.created_at, s.name AS reviewer_name, s.profile_image AS reviewer_image, l.title AS listing_title "
                + "FROM seller_reviews r "
                + "JOIN students s ON s.student_id = r.reviewer_id "
                + "JOIN transactions t ON t.transaction_id = r.transaction_id "
                + "JOIN listings l ON l.listing_id = t.listing_id "
                + "WHERE r.seller_id = ? ORDER BY r.created_at DESC, r.review_id DESC LIMIT ?";
        List<SellerReview> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SellerReview r = new SellerReview();
                    r.setId(rs.getInt("review_id"));
                    r.setSellerId(rs.getInt("seller_id"));
                    r.setReviewerId(rs.getInt("reviewer_id"));
                    r.setTransactionId(rs.getInt("transaction_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(DbUtil.getDateTime(rs, "created_at"));
                    r.setReviewerName(rs.getString("reviewer_name"));
                    r.setReviewerImage(rs.getString("reviewer_image"));
                    r.setListingTitle(rs.getString("listing_title"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    public boolean existsForTransaction(Connection conn, int transactionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM seller_reviews WHERE transaction_id = ?")) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a review; the UNIQUE(transaction_id) constraint rejects duplicates. */
    public int insert(Connection conn, int sellerId, int reviewerId, int transactionId, int rating, String comment)
            throws SQLException {
        String sql = "INSERT INTO seller_reviews (seller_id, reviewer_id, transaction_id, rating, comment) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, reviewerId);
            ps.setInt(3, transactionId);
            ps.setInt(4, rating);
            ps.setString(5, comment);
            ps.executeUpdate();
            return DbUtil.generatedId(ps);
        }
    }
}
