package com.campusloop.service;

import com.campusloop.dao.CartDao;
import com.campusloop.dao.ListingDao;
import com.campusloop.dao.StudentDao;
import com.campusloop.dao.TransactionDao;
import com.campusloop.model.ConfirmationStatus;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingStatus;
import com.campusloop.model.Student;
import com.campusloop.model.Transaction;
import com.campusloop.model.TransactionStatus;
import com.campusloop.model.WalletTxnType;
import com.campusloop.util.DbUtil;
import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Stage 10: secure transactional checkout.
 *
 * Everything below runs in ONE database transaction; any failure rolls all of it
 * back, so a purchase is never left half-done:
 *
 *   1. lock + verify the transaction: buyer is the caller, seller confirmed, awaiting payment
 *   2. lock + verify the listing: RESERVED for this exchange
 *   3. lock both students (ascending id order, so two payments can't deadlock)
 *   4. verify the buyer's wallet covers the amount
 *   5. debit the buyer        (wallet_balance + PURCHASE ledger row)
 *   6. credit the seller      (wallet_balance + SALE_CREDIT ledger row)
 *   7. mark the listing SOLD
 *   8. mark the transaction COMPLETED (completed_at = now)
 *   9. remove the listing from every cart
 *  10. award Impact Points to buyer and seller
 *  11. COMMIT
 */
public class PaymentService {

    private final TransactionDao transactionDao = new TransactionDao();
    private final ListingDao listingDao = new ListingDao();
    private final StudentDao studentDao = new StudentDao();
    private final CartDao cartDao = new CartDao();
    private final WalletService walletService = new WalletService();

    /** Outcome shown to the buyer after a successful payment. */
    public static final class Receipt {
        private final BigDecimal amount;
        private final BigDecimal buyerBalance;
        private final String sellerName;

        Receipt(BigDecimal amount, BigDecimal buyerBalance, String sellerName) {
            this.amount = amount;
            this.buyerBalance = buyerBalance;
            this.sellerName = sellerName;
        }

        public String getAmountLabel() {
            return Format.inr(amount);
        }

        public String getBuyerBalanceLabel() {
            return Format.inr(buyerBalance);
        }

        public String getSellerName() {
            return sellerName;
        }
    }

    public Receipt pay(int buyerId, int transactionId) throws ServiceException, SQLException {
        return DbUtil.inTransaction(conn -> {
            // 1. the exchange itself
            Transaction t = transactionDao.lockById(conn, transactionId);
            if (t == null) {
                throw new NotFoundException("This exchange doesn't exist.");
            }
            if (t.getBuyerId() != buyerId) {
                throw new ForbiddenException("Only the buyer can pay for this exchange.");
            }
            if (t.getBuyerId() == t.getSellerId()) {
                throw new ServiceException("You can't buy your own listing.");
            }
            if (t.getStatus() == TransactionStatus.COMPLETED) {
                throw new ServiceException("This exchange has already been paid.");
            }
            if (t.getStatus() != TransactionStatus.AWAITING_PAYMENT
                    || t.getConfirmationStatus() != ConfirmationStatus.CONFIRMED) {
                throw new ServiceException(t.getStatus() == TransactionStatus.AWAITING_SELLER
                        ? "The seller hasn't confirmed this exchange yet, so it can't be paid."
                        : "This exchange is no longer active, so it can't be paid.");
            }

            // 2. the item
            Listing listing = listingDao.lockById(conn, t.getListingId());
            if (listing == null || listing.getStatus() != ListingStatus.RESERVED) {
                throw new ServiceException("This item is no longer reserved for you, so the payment was stopped.");
            }
            if (listing.getSellerId() != t.getSellerId()) {
                throw new ServiceException("This exchange no longer matches the listing, so the payment was stopped.");
            }

            // 3. both wallets, locked in a fixed order
            Student buyer;
            Student seller;
            if (t.getBuyerId() < t.getSellerId()) {
                buyer = studentDao.lockById(conn, t.getBuyerId());
                seller = studentDao.lockById(conn, t.getSellerId());
            } else {
                seller = studentDao.lockById(conn, t.getSellerId());
                buyer = studentDao.lockById(conn, t.getBuyerId());
            }

            // 4. sufficient balance
            BigDecimal amount = t.getAmount();
            if (buyer.getWalletBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Your wallet has " + Format.inr(buyer.getWalletBalance())
                        + " but this exchange costs " + Format.inr(amount) + ". Add "
                        + Format.inr(amount.subtract(buyer.getWalletBalance()))
                        + " to your wallet and try again. Nothing was charged.");
            }

            // 5 + 6. move the money (each call updates wallet_balance and appends a ledger row)
            BigDecimal buyerAfter = walletService.applyEntry(conn, buyer, amount.negate(), WalletTxnType.PURCHASE,
                    transactionId, "Purchase: " + listing.getTitle());
            walletService.applyEntry(conn, seller, amount, WalletTxnType.SALE_CREDIT,
                    transactionId, "Sale credit: " + listing.getTitle());

            // 7. item sold
            listingDao.updateStatus(conn, listing.getId(), ListingStatus.SOLD);

            // 8. transaction completed (guarded update: exactly one row must change)
            if (transactionDao.markCompleted(conn, transactionId) != 1) {
                throw new ServiceException("The payment could not be completed. Nothing was charged.");
            }
            transactionDao.cancelOtherActiveRequests(conn, listing.getId(), transactionId);

            // 9. out of every cart
            cartDao.deleteByListing(conn, listing.getId());

            // 10. impact points (see ImpactRules)
            studentDao.addImpactPoints(conn, buyer.getId(), ImpactRules.POINTS_PER_COMPLETED_RESALE);
            studentDao.addImpactPoints(conn, seller.getId(), ImpactRules.POINTS_PER_COMPLETED_RESALE);

            return new Receipt(amount, buyerAfter, seller.getName());
        });
    }
}
