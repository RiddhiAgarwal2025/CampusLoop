package com.campusloop.service;

import com.campusloop.config.AppConfig;
import com.campusloop.dao.ListingDao;
import com.campusloop.dao.ReviewDao;
import com.campusloop.dao.StatsDao;
import com.campusloop.dao.StudentDao;
import com.campusloop.dao.TransactionDao;
import com.campusloop.dao.WalletDao;
import com.campusloop.model.ActivityItem;
import com.campusloop.model.DashboardStats;
import com.campusloop.model.DashboardView;
import com.campusloop.model.Listing;
import com.campusloop.model.SellerReview;
import com.campusloop.model.Student;
import com.campusloop.model.Transaction;
import com.campusloop.model.TransactionStatus;
import com.campusloop.model.WalletTransaction;
import com.campusloop.model.WalletTxnType;
import com.campusloop.util.DbUtil;
import com.campusloop.util.Format;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage 13 dashboard. Stats are live COUNT/SUM queries; the activity feed merges
 * real rows from the wallet ledger, exchanges, listings and reviews. Completed
 * purchases and sales appear through their ledger rows (which carry the amount),
 * so no event is shown twice.
 */
public class DashboardService {

    public static final int ACTIVITY_SIZE = 8;
    private static final int SOURCE_LIMIT = 10;

    private final StudentDao studentDao = new StudentDao();
    private final StatsDao statsDao = new StatsDao();
    private final WalletDao walletDao = new WalletDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final ListingDao listingDao = new ListingDao();
    private final ReviewDao reviewDao = new ReviewDao();

    public DashboardView load(int studentId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Student me = studentDao.findById(conn, studentId);
            if (me == null) {
                throw new NotFoundException("Your account could not be found.");
            }

            DashboardStats s = new DashboardStats();
            s.setWalletBalance(me.getWalletBalance());
            s.setImpactPoints(me.getSustainabilityPoints());
            s.setActiveListings(statsDao.listingsBySellerWithStatus(conn, studentId, "AVAILABLE")
                    + statsDao.listingsBySellerWithStatus(conn, studentId, "RESERVED"));
            s.setSoldListings(statsDao.listingsBySellerWithStatus(conn, studentId, "SOLD"));
            s.setCompletedPurchases(statsDao.completedPurchases(conn, studentId));
            s.setCompletedSales(statsDao.completedSales(conn, studentId));
            s.setPendingConfirmations(statsDao.sellerTransactionsWithStatus(conn, studentId, "AWAITING_SELLER"));
            s.setAwaitingPayment(statsDao.buyerTransactionsWithStatus(conn, studentId, "AWAITING_PAYMENT"));
            s.setCompletedResales(statsDao.completedResalesInvolving(conn, studentId));

            List<ActivityItem> feed = new ArrayList<>();
            addLedger(feed, walletDao.listForStudent(conn, studentId, SOURCE_LIMIT));
            addExchanges(feed, transactionDao.listForBuyer(conn, studentId, SOURCE_LIMIT), true);
            addExchanges(feed, transactionDao.listForSeller(conn, studentId, SOURCE_LIMIT), false);
            addListings(feed, listingDao.listBySeller(conn, studentId));
            addReviews(feed, reviewDao.listForSeller(conn, studentId, SOURCE_LIMIT));
            feed.sort(Comparator.comparing(ActivityItem::getWhen, Comparator.nullsLast(Comparator.reverseOrder())));
            List<ActivityItem> recent = new ArrayList<>(feed.subList(0, Math.min(ACTIVITY_SIZE, feed.size())));

            LocalDateTime now = LocalDateTime.now(AppConfig.get().getZoneId());
            return new DashboardView(me, s, recent, greeting(now.getHour()), Format.longDate(now.toLocalDate()),
                    statsDao.itemsRecirculated(conn), ImpactRules.POINTS_PER_COMPLETED_RESALE);
        }
    }

    static String greeting(int hour) {
        if (hour < 5) {
            return "Good evening";
        }
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 17) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    private static void addLedger(List<ActivityItem> feed, List<WalletTransaction> rows) {
        for (WalletTransaction w : rows) {
            String link = w.getTransactionId() == null ? "/wallet" : "/exchange?id=" + w.getTransactionId();
            String kind;
            String title;
            if (w.getType() == WalletTxnType.TOPUP) {
                kind = "topup";
                title = "Added money to your wallet";
            } else if (w.getType() == WalletTxnType.SALE_CREDIT) {
                kind = "sale";
                title = w.getDescription().replaceFirst("^Sale credit: ", "Sold ");
            } else {
                kind = "purchase";
                title = w.getDescription().replaceFirst("^Purchase: ", "Bought ");
            }
            feed.add(new ActivityItem(kind, title, "Balance " + w.getBalanceAfterLabel(), w.getAmountLabel(),
                    w.isCredit(), w.getCreatedAt(), link));
        }
    }

    private static void addExchanges(List<ActivityItem> feed, List<Transaction> rows, boolean asBuyer) {
        for (Transaction t : rows) {
            if (t.getStatus() == TransactionStatus.COMPLETED) {
                continue;
            }
            String link = "/exchange?id=" + t.getId();
            String title;
            String detail;
            LocalDateTime when = t.getTransactionDate();
            switch (t.getStatus()) {
                case AWAITING_SELLER:
                    title = asBuyer ? "You requested " + t.getListingTitle()
                            : t.getBuyerFirstName() + " wants to buy " + t.getListingTitle();
                    detail = asBuyer ? "Waiting for " + t.getSellerFirstName() + " to confirm"
                            : "Confirm or reject it in Sales";
                    break;
                case AWAITING_PAYMENT:
                    title = asBuyer ? t.getSellerFirstName() + " confirmed your exchange"
                            : "You confirmed " + t.getBuyerFirstName() + "'s request";
                    detail = asBuyer ? t.getListingTitle() + " is reserved for you. Ready to pay."
                            : "Waiting for payment for " + t.getListingTitle();
                    if (t.getConfirmedAt() != null) {
                        when = t.getConfirmedAt();
                    }
                    break;
                case REJECTED:
                    title = asBuyer ? "Request for " + t.getListingTitle() + " was declined"
                            : "You declined a request for " + t.getListingTitle();
                    detail = "Nobody was charged";
                    break;
                default:
                    title = "Request for " + t.getListingTitle() + " was cancelled";
                    detail = "Nobody was charged";
                    break;
            }
            feed.add(new ActivityItem("request", title, detail, null, false, when, link));
        }
    }

    private static void addListings(List<ActivityItem> feed, List<Listing> rows) {
        for (Listing l : rows) {
            feed.add(new ActivityItem("listing", "You listed " + l.getTitle(),
                    l.getPriceLabel() + " \u00B7 " + l.getCategoryName(), null, false, l.getCreatedAt(),
                    "/product?id=" + l.getId()));
        }
    }

    private static void addReviews(List<ActivityItem> feed, List<SellerReview> rows) {
        for (SellerReview r : rows) {
            String comment = r.getComment();
            if (comment.length() > 70) {
                comment = comment.substring(0, 69) + "\u2026";
            }
            feed.add(new ActivityItem("review", r.getReviewerFirstName() + " rated you " + r.getRating()
                    + (r.getRating() == 1 ? " star" : " stars"), comment, null, false, r.getCreatedAt(),
                    "/profile#reviews"));
        }
    }
}
