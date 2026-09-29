package com.campusloop.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Aggregate counts. Every statistic shown anywhere in CampusLoop comes from here
 * (or another DAO) and is computed from real rows; nothing is hard-coded.
 */
public class StatsDao {

    /** Items Recirculated = number of COMPLETED transactions (each is one item resold). */
    public int itemsRecirculated(Connection conn) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM transactions WHERE transaction_status = 'COMPLETED'");
    }

    public int availableListings(Connection conn) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM listings WHERE status = 'AVAILABLE'");
    }

    public int registeredStudents(Connection conn) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM students");
    }

    public int listingsBySellerWithStatus(Connection conn, int sellerId, String status) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM listings WHERE seller_id = ? AND status = ?", sellerId, status);
    }

    public int completedPurchases(Connection conn, int buyerId) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM transactions WHERE buyer_id = ? AND transaction_status = 'COMPLETED'",
                buyerId);
    }

    public int completedSales(Connection conn, int sellerId) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM transactions WHERE seller_id = ? AND transaction_status = 'COMPLETED'",
                sellerId);
    }

    /** Transactions on this student's listings in a given status (seller view). */
    public int sellerTransactionsWithStatus(Connection conn, int sellerId, String status) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM transactions WHERE seller_id = ? AND transaction_status = ?",
                sellerId, status);
    }

    /** This student's own requests in a given status (buyer view). */
    public int buyerTransactionsWithStatus(Connection conn, int buyerId, String status) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM transactions WHERE buyer_id = ? AND transaction_status = ?",
                buyerId, status);
    }

    /** Completed resales this student took part in, as buyer or seller. */
    public int completedResalesInvolving(Connection conn, int studentId) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM transactions WHERE transaction_status = 'COMPLETED' "
                + "AND (buyer_id = ? OR seller_id = ?)", studentId, studentId);
    }

    private int count(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) {
                    ps.setInt(i + 1, (Integer) params[i]);
                } else {
                    ps.setString(i + 1, String.valueOf(params[i]));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
