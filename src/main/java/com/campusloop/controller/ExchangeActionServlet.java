package com.campusloop.controller;

import com.campusloop.service.ExchangeService;
import com.campusloop.service.ForbiddenException;
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

/**
 * POST /exchange/confirm  seller confirms (id)
 * POST /exchange/reject   seller declines or releases a reservation (id)
 * POST /exchange/cancel   buyer withdraws (id)
 *
 * The acting student always comes from the session; the service checks that
 * they are the right party for the action.
 */
@WebServlet(name = "ExchangeActionServlet",
        urlPatterns = {"/exchange/confirm", "/exchange/reject", "/exchange/cancel"})
public class ExchangeActionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int me = SessionUtil.requireUserId(req);
        String action = req.getServletPath();
        String back = "/exchange?id=" + id;
        try {
            if ("/exchange/confirm".equals(action)) {
                int cancelled = exchangeService.confirm(me, id);
                SessionUtil.flashSuccess(req, "Exchange confirmed. The item is now reserved for this buyer, who can "
                        + "pay from their wallet." + (cancelled > 0 ? " " + cancelled + " other "
                        + (cancelled == 1 ? "request was" : "requests were") + " closed automatically." : ""));
            } else if ("/exchange/reject".equals(action)) {
                exchangeService.reject(me, id);
                SessionUtil.flashSuccess(req, "Request declined. The buyer has been notified and nobody was charged.");
            } else {
                exchangeService.cancel(me, id);
                SessionUtil.flashSuccess(req, "Your request has been cancelled. Nothing was charged.");
            }
            Redirects.to(req, resp, Redirects.back(req, back));
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, back);
        } catch (SQLException e) {
            throw new ServletException("Could not update the exchange", e);
        }
    }
}
