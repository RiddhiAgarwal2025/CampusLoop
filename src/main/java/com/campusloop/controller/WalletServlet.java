package com.campusloop.controller;

import com.campusloop.service.ServiceException;
import com.campusloop.service.ValidationException;
import com.campusloop.service.WalletService;
import com.campusloop.util.Format;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.SessionUtil;
import com.campusloop.util.Views;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET  /wallet  balance, simulated Add Money, activity ledger
 * POST /wallet  amount (from the custom field or a quick-amount button)
 */
@WebServlet(name = "WalletServlet", urlPatterns = "/wallet")
public class WalletServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int[] QUICK_AMOUNTS = {500, 1000, 2000, 5000};

    private final WalletService walletService = new WalletService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int studentId = SessionUtil.requireUserId(req);
        String amount = Params.str(req, "amount");
        try {
            BigDecimal balance = walletService.topUp(studentId, amount);
            SessionUtil.flashSuccess(req, Format.inr(new BigDecimal(amount.replace(",", "")))
                    + " added to your wallet. New balance: " + Format.inr(balance) + ".");
            Redirects.to(req, resp, "/wallet");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            render(req, resp, amount);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/wallet");
        } catch (SQLException e) {
            throw new ServletException("Top-up failed; nothing was added", e);
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, String amount)
            throws ServletException, IOException {
        try {
            req.setAttribute("wallet", walletService.view(SessionUtil.requireUserId(req)));
        } catch (ServiceException | SQLException e) {
            throw new ServletException("Could not load the wallet", e);
        }
        if (req.getAttribute("errors") == null) {
            req.setAttribute("errors", Collections.emptyMap());
        }
        req.setAttribute("amount", amount);
        Map<Integer, String> quick = new LinkedHashMap<>();
        for (int q : QUICK_AMOUNTS) {
            quick.put(q, Format.inr(BigDecimal.valueOf(q)));
        }
        req.setAttribute("quickAmounts", quick);
        req.setAttribute("minTopUp", WalletService.MIN_TOP_UP.intValue());
        req.setAttribute("maxTopUp", WalletService.MAX_TOP_UP.intValue());
        req.setAttribute("minTopUpLabel", Format.inr(WalletService.MIN_TOP_UP));
        req.setAttribute("maxTopUpLabel", Format.inr(WalletService.MAX_TOP_UP));
        req.setAttribute("pageTitle", "Wallet");
        Views.render(req, resp, "wallet");
    }
}
