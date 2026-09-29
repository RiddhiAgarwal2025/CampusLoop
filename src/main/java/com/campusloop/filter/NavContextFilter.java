package com.campusloop.filter;

import com.campusloop.model.SessionUser;
import com.campusloop.service.CartService;
import com.campusloop.util.GuestCart;
import com.campusloop.util.SessionUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Supplies the header's cart badge (request attribute "navCartCount") on every
 * page: the cart_items count for signed-in students, the cookie count for guests.
 * A failure here never breaks the page; the badge is simply omitted.
 */
public class NavContextFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(NavContextFilter.class.getName());

    private final CartService cartService = new CartService();

    @Override
    public void init(FilterConfig filterConfig) {
        // No configuration required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!SecurityHeadersFilter.isStatic(req)) {
            SessionUser me = SessionUtil.getAuthUser(req);
            if (me != null) {
                try {
                    req.setAttribute("navCartCount", cartService.countForUser(me.getId()));
                } catch (SQLException e) {
                    LOG.log(Level.FINE, "Cart badge unavailable", e);
                }
            } else {
                req.setAttribute("navCartCount", GuestCart.read(req).size());
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
