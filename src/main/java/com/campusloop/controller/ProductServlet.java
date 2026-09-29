package com.campusloop.controller;

import com.campusloop.model.SessionUser;
import com.campusloop.service.MarketService;
import com.campusloop.service.NotFoundException;
import com.campusloop.util.Params;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /product?id=  product detail with seller rating, reviews and Add to Cart. */
@WebServlet(name = "ProductServlet", urlPatterns = "/product")
public class ProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        SessionUser me = SessionUtil.getAuthUser(req);
        try {
            req.setAttribute("view", marketService.product(id, me == null ? null : me.getId()));
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (SQLException e) {
            throw new ServletException("Could not load the product", e);
        }
        req.setAttribute("activeNav", "browse");
        Views.render(req, resp, "product");
    }
}
