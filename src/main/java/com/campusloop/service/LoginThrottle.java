package com.campusloop.service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory brute-force protection for sign-in.
 *
 * After {@link #MAX_FAILURES} failed attempts for the same email (from the same
 * client address) within {@link #WINDOW_MS}, further attempts are refused for
 * {@link #LOCK_MS}. A successful sign-in clears the counter.
 */
public final class LoginThrottle {

    static final int MAX_FAILURES = 5;
    static final long WINDOW_MS = 15L * 60L * 1000L;
    static final long LOCK_MS = 5L * 60L * 1000L;
    private static final int MAX_TRACKED = 10_000;

    private static final Map<String, Attempts> ATTEMPTS = new ConcurrentHashMap<>();

    private LoginThrottle() {
    }

    private static final class Attempts {
        int failures;
        long firstFailureAt;
        long lockedUntil;
    }

    private static String key(String email, String clientAddress) {
        return email + "|" + clientAddress;
    }

    /** Minutes remaining on a lock, or 0 if the attempt may proceed. */
    public static long minutesLocked(String email, String clientAddress) {
        Attempts a = ATTEMPTS.get(key(email, clientAddress));
        if (a == null) {
            return 0;
        }
        synchronized (a) {
            long remaining = a.lockedUntil - System.currentTimeMillis();
            return remaining > 0 ? Math.max(1, (remaining + 59_999) / 60_000) : 0;
        }
    }

    public static void recordFailure(String email, String clientAddress) {
        long now = System.currentTimeMillis();
        if (ATTEMPTS.size() > MAX_TRACKED) {
            purgeExpired(now);
        }
        Attempts a = ATTEMPTS.computeIfAbsent(key(email, clientAddress), k -> new Attempts());
        synchronized (a) {
            if (now - a.firstFailureAt > WINDOW_MS) {
                a.failures = 0;
                a.firstFailureAt = now;
            }
            a.failures++;
            if (a.failures >= MAX_FAILURES) {
                a.lockedUntil = now + LOCK_MS;
                a.failures = 0;
                a.firstFailureAt = now;
            }
        }
    }

    public static void recordSuccess(String email, String clientAddress) {
        ATTEMPTS.remove(key(email, clientAddress));
    }

    private static void purgeExpired(long now) {
        Iterator<Map.Entry<String, Attempts>> it = ATTEMPTS.entrySet().iterator();
        while (it.hasNext()) {
            Attempts a = it.next().getValue();
            synchronized (a) {
                if (a.lockedUntil < now && now - a.firstFailureAt > WINDOW_MS) {
                    it.remove();
                }
            }
        }
    }
}
