package com.campusloop.service;

import com.campusloop.dao.ReviewDao;
import com.campusloop.dao.TransactionDao;
import com.campusloop.model.Transaction;
import com.campusloop.util.DbUtil;
import com.campusloop.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seller reviews (Stage 12). There are no product reviews: only sellers are rated.
 *
 * Rules, all enforced on the server:
 *   - only the buyer of the transaction can review;
 *   - only after it is COMPLETED (i.e. paid);
 *   - one review per transaction (service check + UNIQUE(transaction_id) in the DB);
 *   - nobody can review themselves (service check + CHECK constraint in the DB);
 *   - rating is a whole number 1-5; comment is 10-500 characters.
 */
public class ReviewService {

    public static final int COMMENT_MIN = 10;
    public static final int COMMENT_MAX = 500;

    private final TransactionDao transactionDao = new TransactionDao();
    private final ReviewDao reviewDao = new ReviewDao();

    /** The completed, not-yet-reviewed purchase this buyer may review. */
    public Transaction getReviewable(int buyerId, int transactionId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Transaction t = transactionDao.findById(conn, transactionId);
            checkReviewable(t, buyerId);
            if (t.isReviewed()) {
                throw new ServiceException("You've already reviewed this seller for this purchase.");
            }
            return t;
        }
    }

    /** Stores the review and returns the seller id (so the caller can show the seller's page). */
    public int submit(int buyerId, int transactionId, String ratingText, String commentText)
            throws ServiceException, SQLException {
        Map<String, String> errors = new LinkedHashMap<>();
        int rating = 0;
        String r = Validator.clean(ratingText);
        if (r.matches("[1-5]")) {
            rating = Integer.parseInt(r);
        } else {
            errors.put("rating", "Please choose a rating from 1 to 5 stars.");
        }
        String comment = Validator.clean(commentText);
        if (comment.isEmpty()) {
            errors.put("comment", "Please write a short comment about the exchange.");
        } else if (!Validator.lengthBetween(comment, COMMENT_MIN, COMMENT_MAX)) {
            errors.put("comment", "Your comment must be " + COMMENT_MIN + "-" + COMMENT_MAX + " characters.");
        }
        ValidationException.throwIfAny(errors);

        final int stars = rating;
        try {
            return DbUtil.inTransaction(conn -> {
                Transaction t = transactionDao.lockById(conn, transactionId);
                checkReviewable(t, buyerId);
                if (reviewDao.existsForTransaction(conn, transactionId)) {
                    throw new ServiceException("You've already reviewed this seller for this purchase.");
                }
                reviewDao.insert(conn, t.getSellerId(), buyerId, transactionId, stars, comment);
                return t.getSellerId();
            });
        } catch (SQLException e) {
            if (DbUtil.isDuplicateKey(e)) {
                throw new ServiceException("You've already reviewed this seller for this purchase.");
            }
            throw e;
        }
    }

    private static void checkReviewable(Transaction t, int buyerId) throws ServiceException {
        if (t == null) {
            throw new NotFoundException("This purchase doesn't exist.");
        }
        if (t.getSellerId() == buyerId) {
            throw new ServiceException("You can't review yourself.");
        }
        if (t.getBuyerId() != buyerId) {
            throw new ForbiddenException("Only the buyer of this purchase can review the seller.");
        }
        if (!t.isCompleted()) {
            throw new ServiceException("You can review the seller once the exchange has been paid and completed.");
        }
    }
}
