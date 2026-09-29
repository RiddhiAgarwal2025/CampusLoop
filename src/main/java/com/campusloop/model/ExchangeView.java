package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;

/**
 * Exchange status page model for one participant. The viewer is always the
 * buyer or the seller of the transaction (checked by ExchangeService).
 */
public class ExchangeView {

    private final Transaction transaction;
    private final boolean buyer;
    private final BigDecimal viewerBalance;

    public ExchangeView(Transaction transaction, boolean buyer, BigDecimal viewerBalance) {
        this.transaction = transaction;
        this.buyer = buyer;
        this.viewerBalance = viewerBalance;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public boolean isBuyer() {
        return buyer;
    }

    public boolean isSeller() {
        return !buyer;
    }

    public String getViewerBalanceLabel() {
        return Format.inr(viewerBalance);
    }

    public boolean isCanAfford() {
        return viewerBalance.compareTo(transaction.getAmount()) >= 0;
    }

    public String getShortfallLabel() {
        return Format.inr(transaction.getAmount().subtract(viewerBalance).max(BigDecimal.ZERO));
    }

    public String getBalanceAfterPaymentLabel() {
        return Format.inr(viewerBalance.subtract(transaction.getAmount()));
    }

    /** Stepper: 1 request, 2 seller confirmation, 3 payment, 4 completed. */
    public int getCurrentStep() {
        switch (transaction.getStatus()) {
            case AWAITING_SELLER:
                return 2;
            case AWAITING_PAYMENT:
                return 3;
            case COMPLETED:
                return 5;
            default:
                return transaction.getConfirmedAt() == null ? 2 : 3;
        }
    }

    /** True when the request ended without a sale (rejected or cancelled). */
    public boolean isClosedWithoutSale() {
        return transaction.getStatus() == TransactionStatus.REJECTED
                || transaction.getStatus() == TransactionStatus.CANCELLED;
    }
}
