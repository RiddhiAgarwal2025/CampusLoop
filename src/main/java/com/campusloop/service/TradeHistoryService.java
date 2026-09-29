package com.campusloop.service;

import com.campusloop.dao.TransactionDao;
import com.campusloop.model.TradeHistory;
import com.campusloop.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;

/** Read-only purchases and sales history (Stage 11). Always scoped to the signed-in student. */
public class TradeHistoryService {

    public static final int HISTORY_LIMIT = 200;

    private final TransactionDao transactionDao = new TransactionDao();

    public TradeHistory purchases(int buyerId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return TradeHistory.of(transactionDao.listForBuyer(conn, buyerId, HISTORY_LIMIT), true);
        }
    }

    public TradeHistory sales(int sellerId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return TradeHistory.of(transactionDao.listForSeller(conn, sellerId, HISTORY_LIMIT), false);
        }
    }
}
