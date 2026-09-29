package com.campusloop.controller;

import com.campusloop.model.Listing;
import com.campusloop.model.ListingStatus;
import com.campusloop.service.ListingService;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /my-listings?status=all|available|reserved|sold  the signed-in student's own listings. */
@WebServlet(name = "MyListingsServlet", urlPatterns = "/my-listings")
public class MyListingsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ListingService listingService = new ListingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int sellerId = SessionUtil.requireUserId(req);
        String tab = req.getParameter("status") == null ? "all" : req.getParameter("status").toLowerCase(Locale.ROOT);
        ListingStatus filter = null;
        if ("available".equals(tab)) {
            filter = ListingStatus.AVAILABLE;
        } else if ("reserved".equals(tab)) {
            filter = ListingStatus.RESERVED;
        } else if ("sold".equals(tab)) {
            filter = ListingStatus.SOLD;
        } else {
            tab = "all";
        }
        try {
            List<Listing> all = listingService.myListings(sellerId);
            List<Listing> shown = new ArrayList<>();
            int available = 0;
            int reserved = 0;
            int sold = 0;
            for (Listing l : all) {
                if (l.getStatus() == ListingStatus.AVAILABLE) {
                    available++;
                } else if (l.getStatus() == ListingStatus.RESERVED) {
                    reserved++;
                } else if (l.getStatus() == ListingStatus.SOLD) {
                    sold++;
                }
                if (filter == null || l.getStatus() == filter) {
                    shown.add(l);
                }
            }
            req.setAttribute("listings", shown);
            req.setAttribute("tab", tab);
            req.setAttribute("countAll", all.size());
            req.setAttribute("countAvailable", available);
            req.setAttribute("countReserved", reserved);
            req.setAttribute("countSold", sold);
            req.setAttribute("pendingCounts", listingService.pendingRequestCounts(sellerId));
        } catch (SQLException e) {
            throw new ServletException("Could not load your listings", e);
        }
        req.setAttribute("pageTitle", "My listings");
        Views.render(req, resp, "my-listings");
    }
}
