package com.campusloop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Synchronizer-token CSRF protection.
 * Every POST form includes: &lt;input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"&gt;
 */
public final class CsrfUtil {

    public static final String SESSION_ATTR = "csrfToken";
    public static final String PARAM = "_csrf";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /** Returns the session token, creating session and token if needed. */
    public static String ensureToken(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object token = session.getAttribute(SESSION_ATTR);
        if (token instanceof String) {
            return (String) token;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String fresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_ATTR, fresh);
        return fresh;
    }

    /** Constant-time comparison of the submitted token against the session token. */
    public static boolean isValid(HttpServletRequest req, String submitted) {
        HttpSession session = req.getSession(false);
        if (session == null || submitted == null || submitted.isEmpty()) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTR);
        if (!(expected instanceof String)) {
            return false;
        }
        return MessageDigest.isEqual(
                ((String) expected).getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}
