package com.campusloop.util;

import com.campusloop.model.SessionUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Central place for every HttpSession attribute name and access pattern.
 */
public final class SessionUtil {

    public static final String AUTH_USER = "authUser";
    public static final String FLASH_SUCCESS = "flashSuccess";
    public static final String FLASH_ERROR = "flashError";
    public static final String REDIRECT_AFTER_LOGIN = "redirectAfterLogin";

    private SessionUtil() {
    }

    /** The signed-in student, or null for guests. Never creates a session. */
    public static SessionUser getAuthUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(AUTH_USER);
        return user instanceof SessionUser ? (SessionUser) user : null;
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getAuthUser(req) != null;
    }

    /** Id of the signed-in student; callers behind AuthFilter can rely on it. */
    public static int requireUserId(HttpServletRequest req) {
        SessionUser user = getAuthUser(req);
        if (user == null) {
            throw new IllegalStateException("No authenticated user in session");
        }
        return user.getId();
    }

    /**
     * Signs the student in. The session id is rotated to prevent session
     * fixation; existing attributes (CSRF token, redirect target) are kept.
     */
    public static void login(HttpServletRequest req, SessionUser user) {
        HttpSession existing = req.getSession(false);
        if (existing != null) {
            req.changeSessionId();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute(AUTH_USER, user);
    }

    /** Refreshes the cached user after a profile change (name / photo). */
    public static void updateAuthUser(HttpServletRequest req, SessionUser user) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.setAttribute(AUTH_USER, user);
        }
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static void flashSuccess(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(FLASH_SUCCESS, message);
    }

    public static void flashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(FLASH_ERROR, message);
    }

    public static void rememberRedirect(HttpServletRequest req, String internalPath) {
        if (Redirects.isSafeInternalPath(internalPath)) {
            req.getSession(true).setAttribute(REDIRECT_AFTER_LOGIN, internalPath);
        }
    }

    /** Returns and clears the remembered post-login target, or null. */
    public static String consumeRedirect(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object target = session.getAttribute(REDIRECT_AFTER_LOGIN);
        session.removeAttribute(REDIRECT_AFTER_LOGIN);
        if (target instanceof String && Redirects.isSafeInternalPath((String) target)) {
            return (String) target;
        }
        return null;
    }
}
