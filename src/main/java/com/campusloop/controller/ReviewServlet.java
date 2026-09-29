package com.campusloop.controller;

import com.campusloop.model.Transaction;
import com.campusloop.service.ForbiddenException;
import com.campusloop.service.NotFoundException;
import com.campusloop.service.ReviewService;
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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET  /review?transactionId=  review form for a completed purchase
 * POST /review                 transactionId, rating (1-5), comment
 */
@WebServlet(name = "ReviewServlet", urlPatterns = "/review")
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, true);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, boolean submit)
            throws ServletException, IOException {
        Integer id = Params.id(req, "transactionId");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int me = SessionUtil.requireUserId(req);
        String rating = Params.str(req, "rating");
        String comment = Params.str(req, "comment");
        try {
            if (submit) {
                try {
                    int sellerId = reviewService.submit(me, id, rating, comment);
                    SessionUtil.flashSuccess(req, "Thanks! Your review is live and the seller's rating has been updated.");
                    Redirects.to(req, resp, "/seller?id=" + sellerId);
                    return;
                } catch (ValidationException e) {
                    req.setAttribute("errors", e.getErrors());
                    req.setAttribute("formError", e.getMessage());
                }
            }
            Transaction t = reviewService.getReviewable(me, id);
            if (req.getAttribute("errors") == null) {
                req.setAttribute("errors", Collections.emptyMap());
            }
            req.setAttribute("t", t);
            req.setAttribute("rating", rating);
            req.setAttribute("comment", comment);
            req.setAttribute("pageTitle", "Review " + t.getSellerFirstName());
            Views.render(req, resp, "review");
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/purchases");
        } catch (SQLException e) {
            throw new ServletException("Could not save the review", e);
        }
    }
}
