package com.campusloop.filter;

import com.campusloop.util.Redirects;
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
 * Protects every route listed under "authFilter" in web.xml.
 *
 * Guests are redirected to /login. For GET requests the original page is
 * remembered (as an internal path only) so the student returns there after
 * signing in. Ownership of individual records is enforced by the services,
 * never by this filter alone.
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // No configuration required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (SessionUtil.isLoggedIn(req)) {
            chain.doFilter(request, response);
            return;
        }

        if ("GET".equalsIgnoreCase(req.getMethod())) {
            SessionUtil.rememberRedirect(req, Redirects.currentPath(req));
        }
        SessionUtil.flashError(req, "Please sign in to continue.");
        Redirects.to(req, resp, "/login");
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
