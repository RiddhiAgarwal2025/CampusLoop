package com.campusloop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Guest cart stored in the HttpOnly cookie "cl_guest_cart" as dash-separated
 * listing ids, e.g. "3-9-14" (newest last).
 *
 * The cookie is untrusted input: anything that is not a well-formed list of
 * positive ids is discarded, duplicates are removed, and the list is capped at
 * {@link #MAX_ITEMS}. Every id is re-validated against the database by CartService.
 */
public final class GuestCart {

    public static final int MAX_ITEMS = 20;
    private static final int MAX_COOKIE_LENGTH = 220;

    private GuestCart() {
    }

    /** Listing ids from the cookie in insertion order (possibly empty, never null). */
    public static List<Integer> read(HttpServletRequest req) {
        String raw = CookieUtil.read(req, CookieUtil.GUEST_CART);
        if (raw == null || raw.isEmpty() || raw.length() > MAX_COOKIE_LENGTH || !raw.matches("[0-9-]+")) {
            return Collections.emptyList();
        }
        Set<Integer> ids = new LinkedHashSet<>();
        for (String part : raw.split("-")) {
            Integer id = Params.positiveInt(part);
            if (id != null) {
                ids.add(id);
            }
            if (ids.size() >= MAX_ITEMS) {
                break;
            }
        }
        return new ArrayList<>(ids);
    }

    /** Writes the ids back (or deletes the cookie when the list is empty). */
    public static void write(HttpServletRequest req, HttpServletResponse resp, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            clear(req, resp);
            return;
        }
        StringBuilder sb = new StringBuilder();
        Set<Integer> seen = new LinkedHashSet<>(ids);
        int n = 0;
        for (Integer id : seen) {
            if (n++ >= MAX_ITEMS) {
                break;
            }
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(id);
        }
        CookieUtil.write(req, resp, CookieUtil.GUEST_CART, sb.toString(), CookieUtil.GUEST_CART_MAX_AGE);
    }

    public static void clear(HttpServletRequest req, HttpServletResponse resp) {
        if (CookieUtil.read(req, CookieUtil.GUEST_CART) != null) {
            CookieUtil.delete(req, resp, CookieUtil.GUEST_CART);
        }
    }
}
