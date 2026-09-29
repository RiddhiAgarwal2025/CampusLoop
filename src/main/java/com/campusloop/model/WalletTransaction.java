package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Row of the wallet_transactions ledger. amount is signed (credit +, debit -). */
public class WalletTransaction {

    private int id;
    private int studentId;
    private WalletTxnType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Integer transactionId;
    private String description;
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public WalletTxnType getType() {
        return type;
    }

    public void setType(WalletTxnType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCredit() {
        return amount != null && amount.signum() > 0;
    }

    /** "+Rs350" or "-Rs400" style, using the rupee and minus signs. */
    public String getAmountLabel() {
        if (amount == null) {
            return "";
        }
        return isCredit() ? "+" + Format.inr(amount) : Format.inr(amount);
    }

    public String getBalanceAfterLabel() {
        return Format.inr(balanceAfter);
    }

    public String getCreatedAtLabel() {
        return Format.dateTime(createdAt);
    }
}
