package com.campusloop.controller;

import com.campusloop.service.ForbiddenException;
import com.campusloop.service.ListingService;
import com.campusloop.service.NotFoundException;
import com.campusloop.service.ServiceException;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** POST /listing/remove  soft-deletes one of the signed-in student's AVAILABLE listings. */
@WebServlet(name = "RemoveListingServlet", urlPatterns = "/listing/remove")
public class RemoveListingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ListingService listingService = new ListingService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            int cancelled = listingService.remove(SessionUtil.requireUserId(req), id);
            SessionUtil.flashSuccess(req, cancelled == 0
                    ? "Your listing has been removed from the marketplace."
                    : "Your listing has been removed. " + cancelled + " pending exchange "
                      + (cancelled == 1 ? "request was" : "requests were") + " cancelled; nobody was charged.");
            Redirects.to(req, resp, "/my-listings");
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/my-listings");
        } catch (SQLException e) {
            throw new ServletException("Could not remove the listing", e);
        }
    }
}
