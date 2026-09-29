package com.campusloop.controller;

import com.campusloop.model.CartView;
import com.campusloop.model.SessionUser;
import com.campusloop.service.CartService;
import com.campusloop.service.ServiceException;
import com.campusloop.util.GuestCart;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET  /cart         guest (cookie) or account cart
 * POST /cart/add     listingId
 * POST /cart/remove  listingId
 *
 * Guests can add, view and remove items; their cart moves into their account
 * when they sign in (see LoginServlet).
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart", "/cart/add", "/cart/remove"})
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/cart".equals(req.getServletPath())) {
            Redirects.to(req, resp, "/cart");
            return;
        }
        SessionUser me = SessionUtil.getAuthUser(req);
        CartView cart;
        try {
            if (me != null) {
                cart = cartService.viewForUser(me.getId());
            } else {
                List<Integer> ids = GuestCart.read(req);
                cart = cartService.viewForGuest(ids);
                List<Integer> healed = new ArrayList<>();
                for (int i = cart.getItems().size() - 1; i >= 0; i--) {
                    healed.add(cart.getItems().get(i).getListing().getId());
                }
                if (!healed.equals(ids)) {
                    GuestCart.write(req, resp, healed);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Could not load the cart", e);
        }
        req.setAttribute("cart", cart);
        req.setAttribute("navCartCount", cart.getCount());
        req.setAttribute("pageTitle", "Your cart");
        Views.render(req, resp, "cart");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer listingId = Params.id(req, "listingId");
        if (listingId == null) {
            SessionUtil.flashError(req, "We couldn't find that item.");
            Redirects.to(req, resp, "/cart");
            return;
        }
        boolean adding = "/cart/add".equals(req.getServletPath());
        SessionUser me = SessionUtil.getAuthUser(req);
        try {
            if (adding) {
                add(req, resp, me, listingId);
            } else {
                remove(req, resp, me, listingId);
            }
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, Redirects.back(req, "/cart"));
        } catch (SQLException e) {
            throw new ServletException("Could not update the cart", e);
        }
    }

    private void add(HttpServletRequest req, HttpServletResponse resp, SessionUser me, int listingId)
            throws ServiceException, SQLException, IOException {
        if (me != null) {
            boolean added = cartService.addForUser(me.getId(), listingId);
            SessionUtil.flashSuccess(req, added ? "Added to your cart." : "That item is already in your cart.");
        } else {
            List<Integer> current = GuestCart.read(req);
            boolean already = current.contains(listingId);
            GuestCart.write(req, resp, cartService.addForGuest(current, listingId));
            SessionUtil.flashSuccess(req, already ? "That item is already in your cart."
                    : "Added to your cart. Sign in when you're ready to check out; your cart comes with you.");
        }
        Redirects.to(req, resp, "/cart");
    }

    private void remove(HttpServletRequest req, HttpServletResponse resp, SessionUser me, int listingId)
            throws SQLException, IOException {
        if (me != null) {
            cartService.removeForUser(me.getId(), listingId);
        } else {
            List<Integer> ids = new ArrayList<>(GuestCart.read(req));
            ids.remove(Integer.valueOf(listingId));
            GuestCart.write(req, resp, ids);
        }
        SessionUtil.flashSuccess(req, "Removed from your cart.");
        Redirects.to(req, resp, "/cart");
    }
}
