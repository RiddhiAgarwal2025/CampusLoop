package com.campusloop.controller;

import com.campusloop.model.Student;
import com.campusloop.service.AuthService;
import com.campusloop.service.ServiceException;
import com.campusloop.service.ValidationException;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET  /register  sign-up form
 * POST /register  validate, create the account (PBKDF2 hash), then send the new
 *                 student to /login with their email pre-filled
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(req)) {
            Redirects.to(req, resp, "/dashboard");
            return;
        }
        req.setAttribute("form", new HashMap<String, String>());
        render(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(req)) {
            Redirects.to(req, resp, "/dashboard");
            return;
        }
        String name = Params.str(req, "name");
        String email = Params.str(req, "email");
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        try {
            Student student = authService.register(name, email, password, confirm);
            req.getSession(true).setAttribute(LoginServlet.PREFILL_EMAIL, student.getEmail());
            SessionUtil.flashSuccess(req, "Your CampusLoop account is ready, " + student.getFirstName()
                    + ". Sign in to get started.");
            Redirects.to(req, resp, "/login");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("formError", e.getMessage());
            Map<String, String> form = new HashMap<>();
            form.put("name", name);
            form.put("email", email);
            req.setAttribute("form", form);
            render(req, resp);
        } catch (ServiceException e) {
            req.setAttribute("formError", e.getMessage());
            req.setAttribute("form", new HashMap<String, String>());
            render(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Registration failed because the database is unavailable", e);
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("pageTitle", "Create your account");
        Views.render(req, resp, "register");
    }
}
