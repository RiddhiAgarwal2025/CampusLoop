package com.campusloop.filter;

import com.campusloop.util.SessionUtil;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds defensive HTTP headers to every response. Pages rendered for a signed-in
 * student are marked no-store so wallet/profile data is not cached by the browser.
 */
public class SecurityHeadersFilter implements Filter {

    private static final String CSP =
            "default-src 'self'; "
            + "img-src 'self' data: blob:; "
            + "style-src 'self' 'unsafe-inline'; "
            + "script-src 'self'; "
            + "font-src 'self'; "
            + "form-action 'self'; "
            + "frame-ancestors 'none'; "
            + "base-uri 'self'; "
            + "object-src 'none'";

    @Override
    public void init(FilterConfig filterConfig) {
        // No configuration required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        resp.setHeader("Content-Security-Policy", CSP);

        if (!isStatic(req) && SessionUtil.isLoggedIn(req)) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);
        }
        chain.doFilter(request, response);
    }

    static boolean isStatic(HttpServletRequest req) {
        String path = req.getServletPath();
        return path.startsWith("/assets/") || path.startsWith("/media/") || "/favicon.ico".equals(path);
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
