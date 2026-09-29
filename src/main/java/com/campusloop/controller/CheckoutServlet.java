package com.campusloop.controller;

import com.campusloop.model.ExchangeForm;
import com.campusloop.service.ExchangeService;
import com.campusloop.service.ExchangeService.ExistingRequestException;
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
 * GET  /checkout?listingId=  exchange-details form (location, date, time, note)
 * POST /checkout             listingId + details -> AWAITING_SELLER request (no payment)
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer listingId = Params.id(req, "listingId");
        if (listingId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ExchangeForm form = new ExchangeForm();
        render(req, resp, listingId, form);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer listingId = Params.id(req, "listingId");
        if (listingId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ExchangeForm form = new ExchangeForm();
        form.setLocation(req.getParameter("location"));
        form.setDate(req.getParameter("date"));
        form.setTime(req.getParameter("time"));
        form.setNote(req.getParameter("note"));
        try {
            int id = exchangeService.requestExchange(SessionUtil.requireUserId(req), listingId, form);
            SessionUtil.flashSuccess(req, "Exchange request sent! Nothing has been charged. We'll hold your "
                    + "payment step until the seller confirms.");
            Redirects.to(req, resp, "/exchange?id=" + id);
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("formError", e.getMessage());
            render(req, resp, listingId, form);
        } catch (ExistingRequestException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/exchange?id=" + e.getTransactionId());
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/cart");
        } catch (SQLException e) {
            throw new ServletException("Could not send the exchange request", e);
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, int listingId, ExchangeForm form)
            throws ServletException, IOException {
        try {
            req.setAttribute("checkout", exchangeService.prepareCheckout(SessionUtil.requireUserId(req), listingId));
        } catch (ExistingRequestException e) {
            SessionUtil.flashSuccess(req, e.getMessage());
            Redirects.to(req, resp, "/exchange?id=" + e.getTransactionId());
            return;
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/cart");
            return;
        } catch (SQLException e) {
            throw new ServletException("Could not load checkout", e);
        }
        if (req.getAttribute("errors") == null) {
            req.setAttribute("errors", Collections.emptyMap());
        }
        req.setAttribute("form", form);
        req.setAttribute("pageTitle", "Checkout");
        Views.render(req, resp, "checkout");
    }
}
