package com.campusloop.model;

import com.campusloop.util.Format;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Row of the students table. password_hash never leaves the DAO/service layer. */
public class Student {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String profileImage;
    private BigDecimal walletBalance = BigDecimal.ZERO;
    private int sustainabilityPoints;
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public int getSustainabilityPoints() {
        return sustainabilityPoints;
    }

    public void setSustainabilityPoints(int sustainabilityPoints) {
        this.sustainabilityPoints = sustainabilityPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getWalletLabel() {
        return Format.inr(walletBalance);
    }

    public String getFirstName() {
        return Format.firstName(name);
    }

    public String getMemberSinceLabel() {
        return Format.monthYear(createdAt);
    }

    public String getCreatedAtLabel() {
        return Format.dateTime(createdAt);
    }
}
