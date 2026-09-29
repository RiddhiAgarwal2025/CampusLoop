package com.campusloop.config;

import com.campusloop.service.ImpactRules;
import com.campusloop.util.DbUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application start-up and shut-down.
 *
 * Start-up: loads configuration, registers the JDBC driver, prepares the
 * upload directories and checks the database connection (a failure is logged
 * clearly but does not stop deployment, so the friendly error page can explain it).
 *
 * Shut-down: deregisters JDBC drivers loaded by this web application and stops
 * the MySQL cleanup thread to prevent class-loader memory leaks on redeploy.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        AppConfig config = AppConfig.init();

        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "MySQL JDBC driver not found on the classpath", e);
        }

        prepareUploadDirectories(config.getUploadDir());

        try (Connection conn = DbUtil.getConnection()) {
            if (conn.isValid(2)) {
                LOG.info("CampusLoop connected to the database successfully.");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "CampusLoop could not connect to the database at "
                    + config.getDbUrl() + " as user '" + config.getDbUser()
                    + "'. Import database/campusloop.sql and check campusloop.properties. Cause: "
                    + e.getMessage());
        }

        ctx.setAttribute("appName", "CampusLoop");
        ctx.setAttribute("appTagline", "Good Stuff. Second Chances.");
        ctx.setAttribute("pointsPerResale", ImpactRules.POINTS_PER_COMPLETED_RESALE);
        ctx.setAttribute("maxUploadMb", config.getMaxUploadBytes() / (1024L * 1024L));
        LOG.info("CampusLoop started. Uploads directory: " + config.getUploadDir());
    }

    private void prepareUploadDirectories(Path root) {
        try {
            Files.createDirectories(root.resolve("listings"));
            Files.createDirectories(root.resolve("profiles"));
            if (!Files.isWritable(root)) {
                LOG.severe("Upload directory is not writable: " + root);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Could not create upload directory " + root, e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ClassLoader webappLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == webappLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "Could not deregister JDBC driver " + driver, e);
                }
            }
        }
        try {
            Class<?> cleanup = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            Method shutdown = cleanup.getMethod("checkedShutdown");
            shutdown.invoke(null);
        } catch (ClassNotFoundException e) {
            // Driver not present: nothing to stop.
        } catch (ReflectiveOperationException e) {
            LOG.log(Level.FINE, "MySQL cleanup thread could not be stopped", e);
        }
    }
}
