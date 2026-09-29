package com.campusloop.controller;

import com.campusloop.model.ItemCondition;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingForm;
import com.campusloop.service.ForbiddenException;
import com.campusloop.service.ListingService;
import com.campusloop.service.MarketService;
import com.campusloop.service.NotFoundException;
import com.campusloop.service.ServiceException;
import com.campusloop.service.ValidationException;
import com.campusloop.util.Params;
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

/**
 * GET  /edit?id=  edit form for one of the signed-in student's AVAILABLE listings
 * POST /edit      multipart update; the photo is optional (replaces the current one)
 *
 * Another student's listing id returns 403; a missing id returns 404.
 */
@WebServlet(name = "EditListingServlet", urlPatterns = "/edit")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024)
public class EditListingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ListingService listingService = new ListingService();
    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Listing listing = listingService.getForEdit(SessionUtil.requireUserId(req), id);
            render(req, resp, listing, ListingForm.from(listing));
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/my-listings");
        } catch (SQLException e) {
            throw new ServletException("Could not load the listing", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int sellerId = SessionUtil.requireUserId(req);
        ListingForm form = SellServlet.readForm(req);
        try {
            listingService.update(sellerId, id, form, SellServlet.imagePart(req));
            SessionUtil.flashSuccess(req, "Your changes have been saved.");
            Redirects.to(req, resp, "/product?id=" + id);
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("formError", e.getMessage());
            try {
                render(req, resp, listingService.getForEdit(sellerId, id), form);
            } catch (ServiceException | SQLException inner) {
                throw new ServletException("Could not reload the listing", inner);
            }
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/my-listings");
        } catch (SQLException e) {
            throw new ServletException("Could not update the listing", e);
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, Listing listing, ListingForm form)
            throws ServletException, IOException {
        try {
            req.setAttribute("categories", marketService.categories());
        } catch (SQLException e) {
            throw new ServletException("Could not load categories", e);
        }
        if (req.getAttribute("errors") == null) {
            req.setAttribute("errors", Collections.emptyMap());
        }
        req.setAttribute("listing", listing);
        req.setAttribute("form", form);
        req.setAttribute("conditions", ItemCondition.values());
        req.setAttribute("pageTitle", "Edit listing");
        Views.render(req, resp, "edit");
    }
}
