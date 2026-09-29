package com.campusloop.controller;

import com.campusloop.model.ItemCondition;
import com.campusloop.model.ListingForm;
import com.campusloop.service.ListingService;
import com.campusloop.service.MarketService;
import com.campusloop.service.ServiceException;
import com.campusloop.service.ValidationException;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * GET  /sell  listing form
 * POST /sell  multipart: title, description, categoryId, price, condition, image (mandatory)
 *
 * The container accepts files up to 10 MB so that anything between the 5 MB
 * application limit and 10 MB gets a precise message from ImageStorage.
 */
@WebServlet(name = "SellServlet", urlPatterns = "/sell")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024)
public class SellServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ListingService listingService = new ListingService();
    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, new ListingForm());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int sellerId = SessionUtil.requireUserId(req);
        ListingForm form = readForm(req);
        try {
            int id = listingService.create(sellerId, form, imagePart(req));
            SessionUtil.flashSuccess(req, "Your listing is live! Students can now find it in the marketplace.");
            Redirects.to(req, resp, "/product?id=" + id);
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("formError", e.getMessage());
            render(req, resp, form);
        } catch (ServiceException e) {
            req.setAttribute("formError", e.getMessage());
            render(req, resp, form);
        } catch (SQLException e) {
            throw new ServletException("Could not create the listing", e);
        }
    }

    /** Reads the text fields of the Sell / Edit form (shared with EditListingServlet). */
    static ListingForm readForm(HttpServletRequest req) {
        ListingForm f = new ListingForm();
        f.setTitle(req.getParameter("title"));
        f.setDescription(req.getParameter("description"));
        f.setCategoryId(req.getParameter("categoryId"));
        f.setPrice(req.getParameter("price"));
        f.setCondition(req.getParameter("condition"));
        return f;
    }

    /** The uploaded "image" part, or null if none / the request could not be parsed. */
    static Part imagePart(HttpServletRequest req) throws IOException {
        try {
            return req.getPart("image");
        } catch (ServletException | IllegalStateException e) {
            return null;
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, ListingForm form)
            throws ServletException, IOException {
        try {
            req.setAttribute("categories", marketService.categories());
        } catch (SQLException e) {
            throw new ServletException("Could not load categories", e);
        }
        if (req.getAttribute("errors") == null) {
            req.setAttribute("errors", Collections.emptyMap());
        }
        req.setAttribute("form", form);
        req.setAttribute("conditions", ItemCondition.values());
        req.setAttribute("pageTitle", "Sell an item");
        req.setAttribute("activeNav", "sell");
        Views.render(req, resp, "sell");
    }
}
