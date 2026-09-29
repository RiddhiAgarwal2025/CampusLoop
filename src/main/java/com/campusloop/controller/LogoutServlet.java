package com.campusloop.controller;

import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * POST /logout  invalidates the session (CSRF-protected form in the navigation).
 * GET is not supported, so a link or prefetch can never sign a student out.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUtil.logout(req);
        SessionUtil.flashSuccess(req, "You've been signed out. See you around campus!");
        Redirects.to(req, resp, "/home");
    }
}
