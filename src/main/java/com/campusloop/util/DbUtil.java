package com.campusloop.util;

import com.campusloop.config.AppConfig;
import com.campusloop.service.ServiceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC helpers.
 *
 * Every connection has its session time_zone set to the application time zone so
 * that NOW()/CURRENT_TIMESTAMP and Java's "today" always agree. Date/time columns
 * are read with JDBC 4.2 getObject(..., LocalDateTime.class), which returns the
 * stored wall-clock value without any JVM time-zone shifting.
 */
public final class DbUtil {

    private static final Logger LOG = Logger.getLogger(DbUtil.class.getName());

    private DbUtil() {
    }

    /** Unit of work executed inside one database transaction. */
    public interface TxWork<T> {
        T run(Connection conn) throws SQLException, ServiceException;
    }

    /** Opens a new auto-commit connection. Callers must close it (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        AppConfig cfg = AppConfig.get();
        Connection conn = DriverManager.getConnection(cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
        try (Statement st = conn.createStatement()) {
            st.execute("SET time_zone = '" + currentOffset() + "'");
        } catch (SQLException e) {
            closeQuietly(conn);
            throw e;
        }
        return conn;
    }

    /** "+05:30" style offset of the application zone right now (never user input). */
    private static String currentOffset() {
        ZoneOffset offset = AppConfig.get().getZoneId().getRules().getOffset(java.time.Instant.now());
        String id = offset.getId();
        return "Z".equals(id) ? "+00:00" : id;
    }

    /**
     * Runs {@code work} in a single transaction: commit on success, rollback on
     * any exception. ServiceExceptions (user-facing business failures) are
     * re-thrown unchanged after the rollback.
     */
    public static <T> T inTransaction(TxWork<T> work) throws SQLException, ServiceException {
        try (Connection conn = getConnection()) {
            boolean previous = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T result = work.run(conn);
                conn.commit();
                return result;
            } catch (SQLException | ServiceException | RuntimeException e) {
                rollbackQuietly(conn);
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(previous);
                } catch (SQLException e) {
                    LOG.log(Level.FINE, "Could not restore auto-commit", e);
                }
            }
        }
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Rollback failed", e);
        }
    }

    public static void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            LOG.log(Level.FINE, "Close failed", e);
        }
    }

    /** Last AUTO_INCREMENT key produced by a statement created with RETURN_GENERATED_KEYS. */
    public static int generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No generated key returned");
    }

    public static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }

    public static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDate.class);
    }

    public static LocalTime getTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalTime.class);
    }

    /** True if the SQL error is a unique/primary key violation (SQLState 23000, error 1062). */
    public static boolean isDuplicateKey(SQLException e) {
        return e.getErrorCode() == 1062;
    }

    /** Escapes LIKE wildcards so user search text is matched literally. */
    public static String likeContains(String text) {
        String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
