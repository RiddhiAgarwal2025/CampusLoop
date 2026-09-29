package com.campusloop.service;

import com.campusloop.dao.StudentDao;
import com.campusloop.dao.WalletDao;
import com.campusloop.model.Student;
import com.campusloop.model.WalletTxnType;
import com.campusloop.model.WalletView;
import com.campusloop.util.DbUtil;
import com.campusloop.util.Format;
import com.campusloop.util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * The simulated CampusLoop wallet (academic feature: no real money or gateway).
 *
 * Invariant: a student's wallet_balance changes only inside the same database
 * transaction that appends a wallet_transactions row, while the student row is
 * locked (SELECT ... FOR UPDATE). Therefore wallet_balance always equals the sum
 * of that student's ledger, even with concurrent requests.
 */
public class WalletService {

    public static final BigDecimal MIN_TOP_UP = new BigDecimal("100");
    public static final BigDecimal MAX_TOP_UP = new BigDecimal("10000");
    public static final BigDecimal MAX_BALANCE = new BigDecimal("100000");
    public static final int ACTIVITY_LIMIT = 50;

    private final StudentDao studentDao = new StudentDao();
    private final WalletDao walletDao = new WalletDao();

    /** Adds simulated money. Returns the new balance. Field errors are keyed "amount". */
    public BigDecimal topUp(int studentId, String amountText) throws ServiceException, SQLException {
        String text = Validator.clean(amountText);
        if (text.isEmpty()) {
            throw new ValidationException("amount", "Please enter an amount to add.");
        }
        BigDecimal amount = Validator.parseMoney(text);
        if (amount == null) {
            throw new ValidationException("amount", "Enter an amount in rupees, e.g. 500 or 750.50.");
        }
        if (amount.compareTo(MIN_TOP_UP) < 0 || amount.compareTo(MAX_TOP_UP) > 0) {
            throw new ValidationException("amount", "You can add between " + Format.inr(MIN_TOP_UP) + " and "
                    + Format.inr(MAX_TOP_UP) + " at a time.");
        }
        return DbUtil.inTransaction(conn -> {
            Student me = studentDao.lockById(conn, studentId);
            if (me == null) {
                throw new NotFoundException("Your account could not be found.");
            }
            BigDecimal room = MAX_BALANCE.subtract(me.getWalletBalance());
            if (amount.compareTo(room) > 0) {
                throw new ValidationException("amount", "Your wallet can hold at most " + Format.inr(MAX_BALANCE)
                        + ". You can add up to " + Format.inr(room.max(BigDecimal.ZERO)) + " right now.");
            }
            return applyEntry(conn, me, amount, WalletTxnType.TOPUP, null, "Wallet top-up");
        });
    }

    /**
     * Applies one signed ledger entry to a student whose row the caller has
     * already locked in {@code conn}: updates wallet_balance and appends the
     * ledger row. Used by top-ups and by the checkout payment (debit + credit).
     *
     * @return the balance after the entry
     */
    BigDecimal applyEntry(Connection conn, Student lockedStudent, BigDecimal signedAmount, WalletTxnType type,
                          Integer transactionId, String description) throws ServiceException, SQLException {
        BigDecimal after = lockedStudent.getWalletBalance().add(signedAmount);
        if (after.signum() < 0) {
            throw new ServiceException("Insufficient wallet balance.");
        }
        studentDao.setWalletBalance(conn, lockedStudent.getId(), after);
        walletDao.insert(conn, lockedStudent.getId(), type, signedAmount, after, transactionId,
                truncate(description, 160));
        lockedStudent.setWalletBalance(after);
        return after;
    }

    public WalletView view(int studentId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Student me = studentDao.findById(conn, studentId);
            if (me == null) {
                throw new NotFoundException("Your account could not be found.");
            }
            Map<WalletTxnType, BigDecimal> totals = walletDao.totalsByType(conn, studentId);
            return new WalletView(me.getWalletBalance(),
                    walletDao.listForStudent(conn, studentId, ACTIVITY_LIMIT),
                    totals.get(WalletTxnType.TOPUP),
                    totals.get(WalletTxnType.PURCHASE),
                    totals.get(WalletTxnType.SALE_CREDIT));
        }
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
    }
}
