package com.campusloop.dao;

import com.campusloop.model.Student;
import com.campusloop.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC access to the students table. All statements are parameterised. */
public class StudentDao {

    private static final String COLUMNS =
            "student_id, name, email, password_hash, profile_image, wallet_balance, "
            + "sustainability_points, created_at";

    public Student findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Student findByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailExists(Connection conn, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM students WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new student with a zero wallet and zero points; returns the new id. */
    public int insert(Connection conn, String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO students (name, email, password_hash, wallet_balance, sustainability_points) "
                + "VALUES (?, ?, ?, 0.00, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            return DbUtil.generatedId(ps);
        }
    }

    public void updateName(Connection conn, int id, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE students SET name = ? WHERE student_id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateProfileImage(Connection conn, int id, String imagePath) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE students SET profile_image = ? WHERE student_id = ?")) {
            ps.setString(1, imagePath);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Locks the student row (SELECT ... FOR UPDATE) inside the caller's transaction.
     * Used before any wallet or points change so concurrent requests serialise.
     */
    public Student lockById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students WHERE student_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Sets the wallet balance (caller holds the row lock and writes the ledger row). */
    public void setWalletBalance(Connection conn, int id, BigDecimal balance) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE students SET wallet_balance = ? WHERE student_id = ?")) {
            ps.setBigDecimal(1, balance);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void addImpactPoints(Connection conn, int id, int points) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE students SET sustainability_points = sustainability_points + ? WHERE student_id = ?")) {
            ps.setInt(1, points);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("student_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setProfileImage(rs.getString("profile_image"));
        s.setWalletBalance(rs.getBigDecimal("wallet_balance"));
        s.setSustainabilityPoints(rs.getInt("sustainability_points"));
        s.setCreatedAt(DbUtil.getDateTime(rs, "created_at"));
        return s;
    }
}
