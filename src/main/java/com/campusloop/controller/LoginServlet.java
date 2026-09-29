package com.campusloop.controller;

import com.campusloop.model.CartMigrationResult;
import com.campusloop.model.SessionUser;
import com.campusloop.model.Student;
import com.campusloop.service.AuthService;
import com.campusloop.service.CartService;
import com.campusloop.service.ServiceException;
import com.campusloop.service.ValidationException;
import com.campusloop.util.CookieUtil;
import com.campusloop.util.GuestCart;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Validator;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * GET  /login  sign-in form (email pre-filled from registration or the cl_last_email cookie)
 * POST /login  authenticate, rotate the session id, remember email if requested,
 *              and move the guest cart (cl_guest_cart cookie) into the account cart
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static final String PREFILL_EMAIL = "prefillEmail";

    private final AuthService authService = new AuthService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(req)) {
            Redirects.to(req, resp, "/dashboard");
            return;
        }
        String email = null;
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(PREFILL_EMAIL) instanceof String) {
            email = (String) session.getAttribute(PREFILL_EMAIL);
            session.removeAttribute(PREFILL_EMAIL);
        }
        String remembered = CookieUtil.readEncoded(req, CookieUtil.LAST_EMAIL);
        if (email == null && remembered != null && Validator.isValidEmail(remembered)) {
            email = remembered;
        }
        req.setAttribute("email", email == null ? "" : email);
        req.setAttribute("remember", remembered != null);
        render(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(req)) {
            Redirects.to(req, resp, "/dashboard");
            return;
        }
        String email = Params.str(req, "email");
        String password = req.getParameter("password");
        boolean remember = req.getParameter("remember") != null;

        Student student;
        try {
            student = authService.authenticate(email, password, req.getRemoteAddr());
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            showAgain(req, resp, email, remember, null);
            return;
        } catch (ServiceException e) {
            showAgain(req, resp, email, remember, e.getMessage());
            return;
        } catch (SQLException e) {
            throw new ServletException("Sign-in failed because the database is unavailable", e);
        }

        if (remember) {
            CookieUtil.writeEncoded(req, resp, CookieUtil.LAST_EMAIL, student.getEmail(), CookieUtil.LAST_EMAIL_MAX_AGE);
        } else {
            CookieUtil.delete(req, resp, CookieUtil.LAST_EMAIL);
        }

        SessionUtil.login(req, SessionUser.from(student));

        String welcome = "Welcome back, " + student.getFirstName() + "!";
        List<Integer> guestCart = GuestCart.read(req);
        if (!guestCart.isEmpty()) {
            try {
                CartMigrationResult migrated = cartService.migrateGuestCart(student.getId(), guestCart);
                welcome = welcome + " " + migrated.getSummary();
            } catch (SQLException e) {
                throw new ServletException("Could not move the guest cart", e);
            }
            GuestCart.clear(req, resp);
        }
        SessionUtil.flashSuccess(req, welcome);

        String target = SessionUtil.consumeRedirect(req);
        Redirects.to(req, resp, target != null ? target : "/dashboard");
    }

    private void showAgain(HttpServletRequest req, HttpServletResponse resp, String email,
                           boolean remember, String formError) throws ServletException, IOException {
        req.setAttribute("email", email);
        req.setAttribute("remember", remember);
        if (formError != null) {
            req.setAttribute("formError", formError);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        render(req, resp);
    }

    private void render(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("pageTitle", "Sign in");
        Views.render(req, resp, "login");
    }
}
