package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.util.List;

/** Wallet page model. The balance is read from the database, never computed in the browser. */
public class WalletView {

    private final BigDecimal balance;
    private final List<WalletTransaction> entries;
    private final BigDecimal totalTopUps;
    private final BigDecimal totalSpent;
    private final BigDecimal totalEarned;

    public WalletView(BigDecimal balance, List<WalletTransaction> entries, BigDecimal totalTopUps,
                      BigDecimal totalSpent, BigDecimal totalEarned) {
        this.balance = balance;
        this.entries = entries;
        this.totalTopUps = totalTopUps;
        this.totalSpent = totalSpent;
        this.totalEarned = totalEarned;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getBalanceLabel() {
        return Format.inr(balance);
    }

    public List<WalletTransaction> getEntries() {
        return entries;
    }

    public String getTotalTopUpsLabel() {
        return Format.inr(totalTopUps);
    }

    public String getTotalSpentLabel() {
        return Format.inr(totalSpent);
    }

    public String getTotalEarnedLabel() {
        return Format.inr(totalEarned);
    }
}
