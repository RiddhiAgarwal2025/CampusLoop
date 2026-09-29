package com.campusloop.controller;

import com.campusloop.service.TradeHistoryService;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET /sales  requests for and sales of the signed-in student's items, with
 * pending seller confirmations first (Confirm / Reject post to /exchange/*).
 */
@WebServlet(name = "SalesServlet", urlPatterns = "/sales")
public class SalesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TradeHistoryService historyService = new TradeHistoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("history", historyService.sales(SessionUtil.requireUserId(req)));
        } catch (SQLException e) {
            throw new ServletException("Could not load sales", e);
        }
        req.setAttribute("pageTitle", "Sales");
        Views.render(req, resp, "sales");
    }
}
