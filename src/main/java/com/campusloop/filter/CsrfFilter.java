package com.campusloop.filter;

import com.campusloop.config.AppConfig;
import com.campusloop.util.CsrfUtil;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cross-site request forgery protection.
 *
 * GET (page) requests: guarantees a CSRF token exists in the session so every
 * rendered form can embed it as ${sessionScope.csrfToken}.
 *
 * POST requests: rejects any request whose "_csrf" parameter does not match the
 * session token. Multipart (upload) forms work because Tomcat parses parts for
 * servlets annotated with @MultipartConfig; if the upload itself is too large to
 * parse, the user gets a precise "image too large" message instead.
 */
public class CsrfFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(CsrfFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        // No configuration required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (SecurityHeadersFilter.isStatic(req)) {
            chain.doFilter(request, response);
            return;
        }

        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            CsrfUtil.ensureToken(req);
            chain.doFilter(request, response);
            return;
        }

        String submitted;
        try {
            submitted = req.getParameter(CsrfUtil.PARAM);
        } catch (IllegalStateException e) {
            submitted = null;
        }

        if (submitted == null && isMultipart(req) && uploadTooLarge(req)) {
            CsrfUtil.ensureToken(req);
            long mb = AppConfig.get().getMaxUploadBytes() / (1024L * 1024L);
            SessionUtil.flashError(req, "That image is too large. Please choose a JPG, PNG or WebP file up to "
                    + mb + " MB.");
            Redirects.to(req, resp, Redirects.back(req, "/home"));
            return;
        }

        if (!CsrfUtil.isValid(req, submitted)) {
            LOG.fine("Rejected POST with missing/invalid CSRF token: " + req.getServletPath());
            CsrfUtil.ensureToken(req);
            SessionUtil.flashError(req,
                    "Your session expired or the page was out of date. Please try again.");
            Redirects.to(req, resp, Redirects.back(req, "/home"));
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean isMultipart(HttpServletRequest req) {
        String type = req.getContentType();
        return type != null && type.toLowerCase(java.util.Locale.ROOT).startsWith("multipart/form-data");
    }

    /** True if the multipart body could not be parsed because it exceeds the configured limits. */
    private static boolean uploadTooLarge(HttpServletRequest req) {
        try {
            req.getParts();
            return false;
        } catch (IllegalStateException e) {
            return true;
        } catch (IOException | ServletException e) {
            return false;
        }
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
