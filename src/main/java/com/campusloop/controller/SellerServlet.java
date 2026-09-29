package com.campusloop.controller;

import com.campusloop.model.SellerProfile;
import com.campusloop.service.MarketService;
import com.campusloop.service.NotFoundException;
import com.campusloop.util.Params;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /seller?id=  public seller page: rating, reviews and available items (no private data). */
@WebServlet(name = "SellerServlet", urlPatterns = "/seller")
public class SellerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            SellerProfile profile = marketService.seller(id);
            req.setAttribute("seller", profile);
            req.setAttribute("pageTitle", profile.getName());
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (SQLException e) {
            throw new ServletException("Could not load the seller", e);
        }
        req.setAttribute("activeNav", "browse");
        Views.render(req, resp, "seller");
    }
}
