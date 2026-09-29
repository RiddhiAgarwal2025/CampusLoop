package com.campusloop.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect helpers that only ever target paths inside this application
 * (prevents open redirects through "next", Referer, or session values).
 */
public final class Redirects {

    private Redirects() {
    }

    /** Accepts "/market?q=x" style paths; rejects "//evil", "/\evil", absolute URLs, CR/LF. */
    public static boolean isSafeInternalPath(String path) {
        if (path == null || path.isEmpty() || path.length() > 500) {
            return false;
        }
        if (path.charAt(0) != '/') {
            return false;
        }
        if (path.startsWith("//") || path.startsWith("/\\")) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '\r' || c == '\n' || c == '\\') {
                return false;
            }
        }
        return true;
    }

    /** Redirects to an internal application path such as "/dashboard". */
    public static void to(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        String target = isSafeInternalPath(path) ? path : "/home";
        resp.sendRedirect(req.getContextPath() + target);
    }

    /** Path + query of the current request, relative to the context (for "return here" links). */
    public static String currentPath(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder(req.getServletPath());
        if (req.getPathInfo() != null) {
            sb.append(req.getPathInfo());
        }
        if (req.getQueryString() != null) {
            sb.append('?').append(req.getQueryString());
        }
        return sb.toString();
    }

    /**
     * Internal path of the page the user came from (Referer), or {@code fallback}
     * when the Referer is missing, foreign, or unsafe.
     */
    public static String back(HttpServletRequest req, String fallback) {
        String referer = req.getHeader("Referer");
        if (referer == null) {
            return fallback;
        }
        try {
            URI uri = new URI(referer);
            if (uri.getHost() != null && !uri.getHost().equalsIgnoreCase(req.getServerName())) {
                return fallback;
            }
            String path = uri.getRawPath();
            String ctx = req.getContextPath();
            if (path == null || !path.startsWith(ctx)) {
                return fallback;
            }
            String internal = path.substring(ctx.length());
            if (uri.getRawQuery() != null) {
                internal = internal + "?" + uri.getRawQuery();
            }
            return isSafeInternalPath(internal) ? internal : fallback;
        } catch (URISyntaxException e) {
            return fallback;
        }
    }
}
