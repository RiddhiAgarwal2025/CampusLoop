package com.campusloop.controller;

import com.campusloop.service.ImpactRules;
import com.campusloop.service.MarketService;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /how-it-works  the exchange workflow, wallet and the Impact Point rule explained. */
@WebServlet(name = "HowItWorksServlet", urlPatterns = "/how-it-works")
public class HowItWorksServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("itemsRecirculated", marketService.itemsRecirculated());
        } catch (SQLException e) {
            throw new ServletException("Could not load statistics", e);
        }
        req.setAttribute("impactRule", ImpactRules.RULE_SUMMARY);
        req.setAttribute("pageTitle", "How it works");
        req.setAttribute("activeNav", "how");
        Views.render(req, resp, "how-it-works");
    }
}
