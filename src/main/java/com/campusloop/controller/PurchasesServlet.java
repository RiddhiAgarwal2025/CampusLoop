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

/** GET /purchases  everything the signed-in student bought or requested. */
@WebServlet(name = "PurchasesServlet", urlPatterns = "/purchases")
public class PurchasesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TradeHistoryService historyService = new TradeHistoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("history", historyService.purchases(SessionUtil.requireUserId(req)));
        } catch (SQLException e) {
            throw new ServletException("Could not load purchases", e);
        }
        req.setAttribute("pageTitle", "Purchases");
        Views.render(req, resp, "purchases");
    }
}
