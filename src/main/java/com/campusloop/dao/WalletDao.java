package com.campusloop.dao;

import com.campusloop.model.WalletTxnType;
import com.campusloop.model.WalletTransaction;
import com.campusloop.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC access to the wallet_transactions ledger. Rows are append-only: they are
 * never updated or deleted, so the ledger is a complete audit trail.
 */
public class WalletDao {

    public int insert(Connection conn, int studentId, WalletTxnType type, BigDecimal signedAmount,
                      BigDecimal balanceAfter, Integer transactionId, String description) throws SQLException {
        String sql = "INSERT INTO wallet_transactions (student_id, txn_type, amount, balance_after, transaction_id, "
                + "description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setString(2, type.name());
            ps.setBigDecimal(3, signedAmount);
            ps.setBigDecimal(4, balanceAfter);
            if (transactionId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, transactionId);
            }
            ps.setString(6, description);
            ps.executeUpdate();
            return DbUtil.generatedId(ps);
        }
    }

    /** Most recent ledger rows first. */
    public List<WalletTransaction> listForStudent(Connection conn, int studentId, int limit) throws SQLException {
        String sql = "SELECT wallet_txn_id, student_id, txn_type, amount, balance_after, transaction_id, description, "
                + "created_at FROM wallet_transactions WHERE student_id = ? "
                + "ORDER BY created_at DESC, wallet_txn_id DESC LIMIT ?";
        List<WalletTransaction> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WalletTransaction w = new WalletTransaction();
                    w.setId(rs.getInt("wallet_txn_id"));
                    w.setStudentId(rs.getInt("student_id"));
                    w.setType(WalletTxnType.valueOf(rs.getString("txn_type")));
                    w.setAmount(rs.getBigDecimal("amount"));
                    w.setBalanceAfter(rs.getBigDecimal("balance_after"));
                    int txn = rs.getInt("transaction_id");
                    w.setTransactionId(rs.wasNull() ? null : txn);
                    w.setDescription(rs.getString("description"));
                    w.setCreatedAt(DbUtil.getDateTime(rs, "created_at"));
                    out.add(w);
                }
            }
        }
        return out;
    }

    /** Absolute totals per entry type for one student (e.g. TOPUP -> 3000.00). */
    public Map<WalletTxnType, BigDecimal> totalsByType(Connection conn, int studentId) throws SQLException {
        Map<WalletTxnType, BigDecimal> totals = new EnumMap<>(WalletTxnType.class);
        for (WalletTxnType t : WalletTxnType.values()) {
            totals.put(t, BigDecimal.ZERO);
        }
        String sql = "SELECT txn_type, COALESCE(SUM(ABS(amount)), 0) AS total FROM wallet_transactions "
                + "WHERE student_id = ? GROUP BY txn_type";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totals.put(WalletTxnType.valueOf(rs.getString("txn_type")), rs.getBigDecimal("total"));
                }
            }
        }
        return totals;
    }
}
