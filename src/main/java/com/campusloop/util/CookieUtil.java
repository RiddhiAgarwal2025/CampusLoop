package com.campusloop.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet cookie helpers. All CampusLoop cookies are HttpOnly and scoped to the
 * application context path. Cookie values are always treated as untrusted input.
 */
public final class CookieUtil {

    /** Guest cart: dash-separated listing ids, e.g. "3-9-14". */
    public static final String GUEST_CART = "cl_guest_cart";
    /** Remembered login email (URL-encoded). */
    public static final String LAST_EMAIL = "cl_last_email";

    public static final int GUEST_CART_MAX_AGE = 7 * 24 * 60 * 60;
    public static final int LAST_EMAIL_MAX_AGE = 30 * 24 * 60 * 60;

    private CookieUtil() {
    }

    public static String read(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    public static void write(HttpServletRequest req, HttpServletResponse resp,
                             String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath(cookiePath(req));
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setSecure(req.isSecure());
        resp.addCookie(cookie);
    }

    public static void delete(HttpServletRequest req, HttpServletResponse resp, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath(cookiePath(req));
        cookie.setMaxAge(0);
        cookie.setSecure(req.isSecure());
        resp.addCookie(cookie);
    }

    public static String readEncoded(HttpServletRequest req, String name) {
        String raw = read(req, name);
        if (raw == null) {
            return null;
        }
        try {
            return URLDecoder.decode(raw, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            return null;
        }
    }

    public static void writeEncoded(HttpServletRequest req, HttpServletResponse resp,
                                    String name, String value, int maxAgeSeconds) {
        try {
            write(req, resp, name, URLEncoder.encode(value, "UTF-8"), maxAgeSeconds);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String cookiePath(HttpServletRequest req) {
        String ctx = req.getContextPath();
        return ctx == null || ctx.isEmpty() ? "/" : ctx;
    }
}
