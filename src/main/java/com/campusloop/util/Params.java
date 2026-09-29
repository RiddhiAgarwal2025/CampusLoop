package com.campusloop.util;

import javax.servlet.http.HttpServletRequest;

/** Safe request-parameter parsing. Never throws on malformed input. */
public final class Params {

    private Params() {
    }

    /** Trimmed parameter value, or "" when absent. */
    public static String str(HttpServletRequest req, String name) {
        return Validator.clean(req.getParameter(name));
    }

    /** Positive integer id, or null if absent/invalid/non-positive. */
    public static Integer id(HttpServletRequest req, String name) {
        return positiveInt(req.getParameter(name));
    }

    public static Integer positiveInt(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty() || t.length() > 9) {
            return null;
        }
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) {
                return null;
            }
        }
        int value = Integer.parseInt(t);
        return value > 0 ? value : null;
    }

    /** Integer page number (>= 1); defaults to 1. */
    public static int page(HttpServletRequest req) {
        Integer p = positiveInt(req.getParameter("page"));
        return p == null ? 1 : Math.min(p, 10_000);
    }
}
