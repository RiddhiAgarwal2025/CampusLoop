package com.campusloop.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Server-side validation rules shared by every form. Client-side checks in
 * assets/js/app.js mirror these, but the server never relies on them.
 */
public final class Validator {

    public static final int NAME_MIN = 2;
    public static final int NAME_MAX = 80;
    public static final int EMAIL_MAX = 120;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 64;

    public static final int TITLE_MIN = 5;
    public static final int TITLE_MAX = 100;
    public static final int DESCRIPTION_MIN = 20;
    public static final int DESCRIPTION_MAX = 2000;
    public static final BigDecimal PRICE_MIN = new BigDecimal("10");
    public static final BigDecimal PRICE_MAX = new BigDecimal("100000");

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern NAME =
            Pattern.compile("^[\\p{L}][\\p{L} .'-]*$");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern MONEY = Pattern.compile("^\\d{1,7}(\\.\\d{1,2})?$");

    private Validator() {
    }

    /** Trims and normalises line endings; null becomes "". */
    public static String clean(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= EMAIL_MAX && EMAIL.matcher(email).matches();
    }

    public static boolean isValidName(String name) {
        return name != null
                && name.length() >= NAME_MIN
                && name.length() <= NAME_MAX
                && NAME.matcher(name).matches();
    }

    /** 8-64 characters with at least one letter and one digit. */
    public static String passwordProblem(String password) {
        if (password == null || password.isEmpty()) {
            return "Please enter a password.";
        }
        if (password.length() < PASSWORD_MIN) {
            return "Password must be at least " + PASSWORD_MIN + " characters.";
        }
        if (password.length() > PASSWORD_MAX) {
            return "Password must be at most " + PASSWORD_MAX + " characters.";
        }
        if (!HAS_LETTER.matcher(password).matches() || !HAS_DIGIT.matcher(password).matches()) {
            return "Password must contain at least one letter and one number.";
        }
        return null;
    }

    public static boolean lengthBetween(String s, int min, int max) {
        return s != null && s.length() >= min && s.length() <= max;
    }

    /**
     * Parses a rupee amount with at most two decimals ("450", "450.5", "450.50").
     * Returns null if the text is not a plain positive number.
     */
    public static BigDecimal parseMoney(String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim().replace(",", "");
        if (!MONEY.matcher(t).matches()) {
            return null;
        }
        return new BigDecimal(t).setScale(2, java.math.RoundingMode.UNNECESSARY);
    }

    public static boolean isValidPrice(BigDecimal price) {
        return price != null && price.compareTo(PRICE_MIN) >= 0 && price.compareTo(PRICE_MAX) <= 0;
    }
}
