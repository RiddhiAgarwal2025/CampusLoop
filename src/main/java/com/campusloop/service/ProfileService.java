package com.campusloop.service;

import com.campusloop.dao.ReviewDao;
import com.campusloop.dao.StatsDao;
import com.campusloop.dao.StudentDao;
import com.campusloop.model.ProfileView;
import com.campusloop.model.Student;
import com.campusloop.util.DbUtil;
import com.campusloop.util.ImageStorage;
import com.campusloop.util.Validator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.http.Part;

/**
 * Stage 16 profile. A student can only ever read or change their own profile:
 * the id always comes from the session, never from the request.
 */
public class ProfileService {

    private final StudentDao studentDao = new StudentDao();
    private final StatsDao statsDao = new StatsDao();
    private final ReviewDao reviewDao = new ReviewDao();

    public ProfileView view(int studentId) throws ServiceException, SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            Student me = studentDao.findById(conn, studentId);
            if (me == null) {
                throw new NotFoundException("Your account could not be found.");
            }
            return new ProfileView(me, reviewDao.ratingForSeller(conn, studentId),
                    statsDao.completedSales(conn, studentId),
                    statsDao.completedPurchases(conn, studentId),
                    statsDao.listingsBySellerWithStatus(conn, studentId, "AVAILABLE")
                            + statsDao.listingsBySellerWithStatus(conn, studentId, "RESERVED"),
                    reviewDao.listForSeller(conn, studentId, 50));
        }
    }

    /**
     * Updates the display name and, if a file was chosen, the profile photo
     * (validated and stored like listing photos; the previous upload is deleted
     * after the database update succeeds). Returns the updated student.
     */
    public Student update(int studentId, String rawName, Part photo)
            throws ServiceException, SQLException, IOException {
        String name = Validator.clean(rawName).replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new ValidationException("name", "Please enter your name.");
        }
        if (!Validator.isValidName(name)) {
            throw new ValidationException("name", "Use 2-80 letters (spaces, apostrophes, dots and hyphens are fine).");
        }
        String newPhoto = ImageStorage.hasFile(photo)
                ? ImageStorage.store(photo, ImageStorage.Kind.PROFILES, "photo") : null;
        String oldPhoto;
        try (Connection conn = DbUtil.getConnection()) {
            Student me = studentDao.findById(conn, studentId);
            if (me == null) {
                throw new NotFoundException("Your account could not be found.");
            }
            oldPhoto = me.getProfileImage();
            studentDao.updateName(conn, studentId, name);
            if (newPhoto != null) {
                studentDao.updateProfileImage(conn, studentId, newPhoto);
            }
        } catch (ServiceException | SQLException e) {
            ImageStorage.deleteQuietly(newPhoto);
            throw e;
        }
        if (newPhoto != null) {
            ImageStorage.deleteQuietly(oldPhoto);
        }
        try (Connection conn = DbUtil.getConnection()) {
            return studentDao.findById(conn, studentId);
        }
    }

    /** Removes the profile photo (initials are shown instead). Returns the updated student. */
    public Student removePhoto(int studentId) throws ServiceException, SQLException {
        String oldPhoto;
        Student updated;
        try (Connection conn = DbUtil.getConnection()) {
            Student me = studentDao.findById(conn, studentId);
            if (me == null) {
                throw new NotFoundException("Your account could not be found.");
            }
            oldPhoto = me.getProfileImage();
            studentDao.updateProfileImage(conn, studentId, null);
            updated = studentDao.findById(conn, studentId);
        }
        ImageStorage.deleteQuietly(oldPhoto);
        return updated;
    }
}
