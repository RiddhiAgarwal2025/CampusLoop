package com.campusloop.service;

import com.campusloop.config.AppConfig;
import com.campusloop.dao.CartDao;
import com.campusloop.dao.ListingDao;
import com.campusloop.dao.ReviewDao;
import com.campusloop.dao.StudentDao;
import com.campusloop.dao.TransactionDao;
import com.campusloop.model.CheckoutView;
import com.campusloop.model.ExchangeForm;
import com.campusloop.model.ExchangeView;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingStatus;
import com.campusloop.model.Student;
import com.campusloop.model.Transaction;
import com.campusloop.model.TransactionStatus;
import com.campusloop.util.DbUtil;
import com.campusloop.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The exchange workflow (Stages 8 and 9):
 *
 *   PRODUCT -> CART -> CHECKOUT (exchange details) -> AWAITING_SELLER
 *        seller confirms -> AWAITING_PAYMENT (listing RESERVED, competing requests cancelled)
 *        seller rejects  -> REJECTED
 *        buyer cancels   -> CANCELLED
 *
 * No money moves here. Payment happens only in PaymentService, and only after
 * the seller's confirmation has been written to the database.
 *
 * Locking order (same as PaymentService): transaction -> listing -> students.
 */
public class ExchangeService {

    public static final int MAX_DAYS_AHEAD = 30;
    public static final LocalTime EARLIEST = LocalTime.of(7, 0);
    public static final LocalTime LATEST = LocalTime.of(21, 30);
    public static final int MIN_LEAD_MINUTES = 30;
    public static final int LOCATION_MIN = 3;
    public static final int LOCATION_MAX = 120;
    public static final int NOTE_MAX = 300;

    /** Suggestions only; any campus location may be typed. */
    public static final List<String> SUGGESTED_LOCATIONS = Collections.unmodifiableList(Arrays.asList(
            "Central Library Entrance", "Main Canteen", "Academic Block A Lobby", "Student Activity Centre",
            "Main Gate Security Desk", "Sports Complex Entrance", "Boys Hostel Parking", "Girls Hostel Block B Gate"));

    private final ListingDao listingDao = new ListingDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final StudentDao studentDao = new StudentDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final CartDao cartDao = new CartDao();

    // ------------------------------------------------------------------ checkout (buyer)

    /**
     * Validates that {@code buyerId} may start an exchange for this listing and
     * returns the checkout page model. If the buyer already has an in-progress
     * request for it, {@link ExistingRequestException} carries its id.
     */
    public CheckoutView prepareCheckout(int buyerId, int listingId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Listing listing = listingDao.findById(conn, listingId);
            Transaction active = transactionDao.findActiveForBuyerAndListing(conn, buyerId, listingId);
            if (active != null) {
                throw new ExistingRequestException(active.getId());
            }
            requireBuyable(listing, buyerId);
            Student buyer = studentDao.findById(conn, buyerId);
            LocalDate today = today();
            return new CheckoutView(listing, reviewDao.ratingForSeller(conn, listing.getSellerId()),
                    buyer.getWalletBalance(), today.toString(), today.plusDays(MAX_DAYS_AHEAD).toString(),
                    EARLIEST.toString(), LATEST.toString(), SUGGESTED_LOCATIONS);
        }
    }

    /**
     * Stores the buyer's exchange proposal (location, date, time, note) as an
     * AWAITING_SELLER transaction. The amount is the listing price at this moment.
     * Returns the new transaction id.
     */
    public int requestExchange(int buyerId, int listingId, ExchangeForm form) throws ServiceException, SQLException {
        Map<String, String> errors = new LinkedHashMap<>();

        String location = Validator.clean(form.getLocation()).replaceAll("\\s+", " ");
        if (location.isEmpty()) {
            errors.put("location", "Please choose where on campus to meet.");
        } else if (!Validator.lengthBetween(location, LOCATION_MIN, LOCATION_MAX)) {
            errors.put("location", "The location must be " + LOCATION_MIN + "-" + LOCATION_MAX + " characters.");
        }

        LocalDate today = today();
        LocalDate date = null;
        try {
            date = LocalDate.parse(Validator.clean(form.getDate()));
        } catch (DateTimeParseException e) {
            errors.put("date", "Please choose a date.");
        }
        if (date != null && (date.isBefore(today) || date.isAfter(today.plusDays(MAX_DAYS_AHEAD)))) {
            errors.put("date", "Choose a date between today and " + MAX_DAYS_AHEAD + " days from now.");
        }

        LocalTime time = null;
        try {
            time = LocalTime.parse(Validator.clean(form.getTime()));
        } catch (DateTimeParseException e) {
            errors.put("time", "Please choose a time.");
        }
        if (time != null) {
            time = time.withSecond(0).withNano(0);
            if (time.isBefore(EARLIEST) || time.isAfter(LATEST)) {
                errors.put("time", "Choose a time between 7:00 AM and 9:30 PM.");
            } else if (date != null && date.equals(today)
                    && time.isBefore(LocalTime.now(AppConfig.get().getZoneId()).plusMinutes(MIN_LEAD_MINUTES))) {
                errors.put("time", "For today, choose a time at least " + MIN_LEAD_MINUTES + " minutes from now.");
            }
        }

        String note = Validator.clean(form.getNote());
        if (note.length() > NOTE_MAX) {
            errors.put("note", "The note can be at most " + NOTE_MAX + " characters.");
        }
        ValidationException.throwIfAny(errors);

        final LocalDate exchangeDate = date;
        final LocalTime exchangeTime = time;
        final String exchangeNote = note.isEmpty() ? null : note;
        return DbUtil.inTransaction(conn -> {
            Listing listing = listingDao.lockById(conn, listingId);
            requireBuyable(listing, buyerId);
            Transaction active = transactionDao.findActiveForBuyerAndListing(conn, buyerId, listingId);
            if (active != null) {
                throw new ExistingRequestException(active.getId());
            }
            int id = transactionDao.insert(conn, buyerId, listing.getSellerId(), listingId, listing.getPrice(),
                    location, exchangeDate, exchangeTime, exchangeNote);
            cartDao.add(conn, buyerId, listingId);
            return id;
        });
    }

    // ------------------------------------------------------------------ viewing

    /** Exchange page model; only the buyer or the seller of this transaction may see it. */
    public ExchangeView view(int viewerId, int transactionId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Transaction t = transactionDao.findById(conn, transactionId);
            if (t == null) {
                throw new NotFoundException("This exchange doesn't exist.");
            }
            if (t.getBuyerId() != viewerId && t.getSellerId() != viewerId) {
                throw new ForbiddenException("This exchange belongs to other students.");
            }
            Student me = studentDao.findById(conn, viewerId);
            return new ExchangeView(t, t.getBuyerId() == viewerId, me.getWalletBalance());
        }
    }

    // ------------------------------------------------------------------ seller actions (Stage 9)

    /**
     * Seller confirms a pending request: the listing becomes RESERVED for this
     * buyer and every other open request for it is cancelled.
     *
     * @return the number of competing requests that were cancelled
     */
    public int confirm(int sellerId, int transactionId) throws ServiceException, SQLException {
        return DbUtil.inTransaction(conn -> {
            Transaction t = lockOwnedBySeller(conn, sellerId, transactionId);
            if (t.getStatus() != TransactionStatus.AWAITING_SELLER) {
                throw new ServiceException(alreadyHandled(t.getStatus()));
            }
            if (t.getExchangeDate().isBefore(today())) {
                throw new ServiceException("The proposed exchange date has already passed. Decline this request "
                        + "so the buyer can propose a new time.");
            }
            Listing listing = listingDao.lockById(conn, t.getListingId());
            if (listing == null || listing.getStatus() != ListingStatus.AVAILABLE) {
                throw new ServiceException("This item is no longer available, so the request can't be confirmed.");
            }
            listingDao.updateStatus(conn, listing.getId(), ListingStatus.RESERVED);
            if (transactionDao.markConfirmed(conn, transactionId) != 1) {
                throw new ServiceException("This request could not be confirmed. Please refresh and try again.");
            }
            return transactionDao.cancelOtherActiveRequests(conn, listing.getId(), transactionId);
        });
    }

    /**
     * Seller declines a pending request, or releases a confirmed reservation
     * before payment (the item becomes AVAILABLE again). Nobody is charged.
     */
    public void reject(int sellerId, int transactionId) throws ServiceException, SQLException {
        DbUtil.inTransaction(conn -> {
            Transaction t = lockOwnedBySeller(conn, sellerId, transactionId);
            if (!t.getStatus().isActive()) {
                throw new ServiceException(alreadyHandled(t.getStatus()));
            }
            releaseReservationIfHeld(conn, t);
            if (transactionDao.markRejected(conn, transactionId) != 1) {
                throw new ServiceException("This request could not be declined. Please refresh and try again.");
            }
            return null;
        });
    }

    // ------------------------------------------------------------------ buyer action

    /** Buyer withdraws a request (before or after confirmation, always before payment). */
    public void cancel(int buyerId, int transactionId) throws ServiceException, SQLException {
        DbUtil.inTransaction(conn -> {
            Transaction t = transactionDao.lockById(conn, transactionId);
            if (t == null) {
                throw new NotFoundException("This exchange doesn't exist.");
            }
            if (t.getBuyerId() != buyerId) {
                throw new ForbiddenException("Only the buyer can cancel this request.");
            }
            if (!t.getStatus().isActive()) {
                throw new ServiceException(alreadyHandled(t.getStatus()));
            }
            releaseReservationIfHeld(conn, t);
            if (transactionDao.markCancelled(conn, transactionId) != 1) {
                throw new ServiceException("This request could not be cancelled. Please refresh and try again.");
            }
            return null;
        });
    }

    // ------------------------------------------------------------------ helpers

    static LocalDate today() {
        return LocalDateTime.now(AppConfig.get().getZoneId()).toLocalDate();
    }

    private Transaction lockOwnedBySeller(Connection conn, int sellerId, int transactionId)
            throws ServiceException, SQLException {
        Transaction t = transactionDao.lockById(conn, transactionId);
        if (t == null) {
            throw new NotFoundException("This exchange doesn't exist.");
        }
        if (t.getSellerId() != sellerId) {
            throw new ForbiddenException("Only the seller can respond to this request.");
        }
        return t;
    }

    /** A confirmed (AWAITING_PAYMENT) request holds the listing as RESERVED; give it back. */
    private void releaseReservationIfHeld(Connection conn, Transaction t) throws SQLException {
        if (t.getStatus() == TransactionStatus.AWAITING_PAYMENT) {
            Listing listing = listingDao.lockById(conn, t.getListingId());
            if (listing != null && listing.getStatus() == ListingStatus.RESERVED) {
                listingDao.updateStatus(conn, listing.getId(), ListingStatus.AVAILABLE);
            }
        }
    }

    private static void requireBuyable(Listing listing, int buyerId) throws ServiceException {
        if (listing == null || listing.getStatus() == ListingStatus.REMOVED) {
            throw new NotFoundException("This item doesn't exist or has been removed by its seller.");
        }
        if (listing.getSellerId() == buyerId) {
            throw new ServiceException("You can't buy your own listing.");
        }
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new ServiceException("Sorry, this item has already been sold.");
        }
        if (listing.getStatus() == ListingStatus.RESERVED) {
            throw new ServiceException("This item is reserved for another student's confirmed exchange.");
        }
    }

    private static String alreadyHandled(TransactionStatus status) {
        switch (status) {
            case AWAITING_PAYMENT:
                return "This request has already been confirmed.";
            case COMPLETED:
                return "This exchange is already completed and paid.";
            case REJECTED:
                return "This request was declined by the seller.";
            case CANCELLED:
                return "This request was cancelled.";
            default:
                return "This request can't be changed right now.";
        }
    }

    /** Thrown when the buyer already has an in-progress request for the listing. */
    public static final class ExistingRequestException extends ServiceException {
        private static final long serialVersionUID = 1L;
        private final int transactionId;

        public ExistingRequestException(int transactionId) {
            super("You already have an exchange request in progress for this item.");
            this.transactionId = transactionId;
        }

        public int getTransactionId() {
            return transactionId;
        }
    }
}
