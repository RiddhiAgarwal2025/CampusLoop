package com.campusloop.util;

import com.campusloop.config.AppConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Display formatting. Models expose "...Label" getters built with these helpers,
 * because JSTL's fmt tags do not support java.time types.
 */
public final class Format {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private Format() {
    }

    /** Indian-style rupees: Rs 2,450 / 1,00,000 / 349.50 with the rupee sign (paise shown only when non-zero). */
    public static String inr(BigDecimal amount) {
        if (amount == null) {
            return "\u20B90";
        }
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        boolean negative = value.signum() < 0;
        value = value.abs();
        String plain = value.toPlainString();
        int dot = plain.indexOf('.');
        String whole = plain.substring(0, dot);
        String paise = plain.substring(dot + 1);

        StringBuilder grouped = new StringBuilder();
        int len = whole.length();
        if (len <= 3) {
            grouped.append(whole);
        } else {
            String head = whole.substring(0, len - 3);
            String tail = whole.substring(len - 3);
            StringBuilder h = new StringBuilder();
            int count = 0;
            for (int i = head.length() - 1; i >= 0; i--) {
                h.append(head.charAt(i));
                count++;
                if (count % 2 == 0 && i > 0) {
                    h.append(',');
                }
            }
            grouped.append(h.reverse()).append(',').append(tail);
        }
        StringBuilder out = new StringBuilder();
        if (negative) {
            out.append('\u2212'); // minus sign
        }
        out.append('\u20B9').append(grouped);
        if (!"00".equals(paise)) {
            out.append('.').append(paise);
        }
        return out.toString();
    }

    public static String date(LocalDate d) {
        return d == null ? "" : DATE.format(d);
    }

    public static String longDate(LocalDate d) {
        return d == null ? "" : LONG_DATE.format(d);
    }

    public static String time(LocalTime t) {
        return t == null ? "" : TIME.format(t);
    }

    public static String dateTime(LocalDateTime dt) {
        return dt == null ? "" : DATE_TIME.format(dt);
    }

    public static String monthYear(LocalDateTime dt) {
        return dt == null ? "" : MONTH_YEAR.format(dt);
    }

    /** "just now", "5 min ago", "3 hours ago", "yesterday", "4 days ago", else a date. */
    public static String relative(LocalDateTime dt) {
        if (dt == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now(AppConfig.get().getZoneId());
        Duration d = Duration.between(dt, now);
        if (d.isNegative()) {
            return dateTime(dt);
        }
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        }
        long days = d.toDays();
        if (days == 1) {
            return "yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }
        return date(dt.toLocalDate());
    }

    /** One decimal place, e.g. 4.5 */
    public static String rating(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    public static String firstName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        return name.trim().split("\\s+")[0];
    }
}
