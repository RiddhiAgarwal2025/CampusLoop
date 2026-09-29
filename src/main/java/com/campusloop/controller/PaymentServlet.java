package com.campusloop.controller;

import com.campusloop.service.ForbiddenException;
import com.campusloop.service.ImpactRules;
import com.campusloop.service.NotFoundException;
import com.campusloop.service.PaymentService;
import com.campusloop.service.ServiceException;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** POST /pay  (id) pays a seller-confirmed exchange from the buyer's wallet in one DB transaction. */
@WebServlet(name = "PaymentServlet", urlPatterns = "/pay")
public class PaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PaymentServlet.class.getName());

    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String back = "/exchange?id=" + id;
        try {
            PaymentService.Receipt receipt = paymentService.pay(SessionUtil.requireUserId(req), id);
            SessionUtil.flashSuccess(req, "Payment complete! " + receipt.getAmountLabel() + " was paid to "
                    + receipt.getSellerName() + ". Your new balance is " + receipt.getBuyerBalanceLabel()
                    + ", and you both earned " + ImpactRules.POINTS_PER_COMPLETED_RESALE + " Impact Points.");
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Payment rolled back for exchange " + id, e);
            SessionUtil.flashError(req, "The payment could not be completed and has been rolled back. "
                    + "Nothing was charged. Please try again.");
        }
        Redirects.to(req, resp, back);
    }
}
