package com.campusloop.controller;

import com.campusloop.service.DashboardService;
import com.campusloop.service.ServiceException;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /dashboard  greeting, live stats, things needing attention, recent activity, impact. */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("dash", dashboardService.load(SessionUtil.requireUserId(req)));
        } catch (ServiceException | SQLException e) {
            throw new ServletException("Could not load the dashboard", e);
        }
        req.setAttribute("pageTitle", "Dashboard");
        Views.render(req, resp, "dashboard");
    }
}
