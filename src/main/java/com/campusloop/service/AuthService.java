package com.campusloop.service;

import com.campusloop.dao.StudentDao;
import com.campusloop.model.Student;
import com.campusloop.util.DbUtil;
import com.campusloop.util.PasswordUtil;
import com.campusloop.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registration and sign-in rules. */
public class AuthService {

    private static final String BAD_CREDENTIALS = "Incorrect email or password.";

    private final StudentDao studentDao = new StudentDao();

    /**
     * Validates and creates an account. Returns the new student.
     * Field errors are keyed: name, email, password, confirmPassword.
     */
    public Student register(String rawName, String rawEmail, String password, String confirmPassword)
            throws ServiceException, SQLException {
        String name = Validator.clean(rawName).replaceAll("\\s+", " ");
        String email = Validator.normalizeEmail(rawEmail);
        Map<String, String> errors = new LinkedHashMap<>();

        if (name.isEmpty()) {
            errors.put("name", "Please enter your name.");
        } else if (!Validator.isValidName(name)) {
            errors.put("name", "Use 2-80 letters (spaces, apostrophes, dots and hyphens are fine).");
        }

        if (email.isEmpty()) {
            errors.put("email", "Please enter your email address.");
        } else if (!Validator.isValidEmail(email)) {
            errors.put("email", "Please enter a valid email address, e.g. name@college.edu.");
        }

        String passwordProblem = Validator.passwordProblem(password);
        if (passwordProblem != null) {
            errors.put("password", passwordProblem);
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            errors.put("confirmPassword", "Please confirm your password.");
        } else if (password != null && !password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match.");
        }

        try (Connection conn = DbUtil.getConnection()) {
            if (!errors.containsKey("email") && studentDao.emailExists(conn, email)) {
                errors.put("email", "An account with this email already exists. Try signing in.");
            }
            ValidationException.throwIfAny(errors);

            String hash = PasswordUtil.hash(password);
            int id;
            try {
                id = studentDao.insert(conn, name, email, hash);
            } catch (SQLException e) {
                if (DbUtil.isDuplicateKey(e)) {
                    // Another request registered the same email a moment ago.
                    throw new ValidationException("email",
                            "An account with this email already exists. Try signing in.");
                }
                throw e;
            }
            return studentDao.findById(conn, id);
        }
    }

    /**
     * Verifies credentials. The same generic message is used for unknown email
     * and wrong password so accounts cannot be enumerated.
     */
    public Student authenticate(String rawEmail, String password, String clientAddress)
            throws ServiceException, SQLException {
        String email = Validator.normalizeEmail(rawEmail);
        Map<String, String> errors = new LinkedHashMap<>();
        if (email.isEmpty()) {
            errors.put("email", "Please enter your email address.");
        } else if (!Validator.isValidEmail(email)) {
            errors.put("email", "Please enter a valid email address.");
        }
        if (password == null || password.isEmpty()) {
            errors.put("password", "Please enter your password.");
        }
        ValidationException.throwIfAny(errors);

        long locked = LoginThrottle.minutesLocked(email, clientAddress);
        if (locked > 0) {
            throw new ServiceException("Too many failed attempts. Please wait " + locked
                    + (locked == 1 ? " minute" : " minutes") + " and try again.");
        }

        Student student;
        try (Connection conn = DbUtil.getConnection()) {
            student = studentDao.findByEmail(conn, email);
        }
        if (student == null) {
            PasswordUtil.verifyDummy(password);
            LoginThrottle.recordFailure(email, clientAddress);
            throw new ServiceException(BAD_CREDENTIALS);
        }
        if (!PasswordUtil.verify(password, student.getPasswordHash())) {
            LoginThrottle.recordFailure(email, clientAddress);
            throw new ServiceException(BAD_CREDENTIALS);
        }
        LoginThrottle.recordSuccess(email, clientAddress);
        return student;
    }
}
