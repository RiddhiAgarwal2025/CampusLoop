package com.campusloop.controller;

import com.campusloop.service.MarketService;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /home  hero, search, live "Items Recirculated", categories and trending items. */
@WebServlet(name = "HomeServlet", urlPatterns = "/home")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("home", marketService.home());
        } catch (SQLException e) {
            throw new ServletException("Could not load the home page", e);
        }
        req.setAttribute("bodyClass", "page-home");
        Views.render(req, resp, "home");
    }
}
