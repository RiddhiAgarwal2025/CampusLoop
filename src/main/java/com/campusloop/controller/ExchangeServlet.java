package com.campusloop.controller;

import com.campusloop.service.ExchangeService;
import com.campusloop.service.ForbiddenException;
import com.campusloop.service.NotFoundException;
import com.campusloop.service.ServiceException;
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

/** GET /exchange?id=  status page for one exchange; only its buyer and seller may open it. */
@WebServlet(name = "ExchangeServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = Params.id(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            req.setAttribute("view", exchangeService.view(SessionUtil.requireUserId(req), id));
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (ServiceException | SQLException e) {
            throw new ServletException("Could not load the exchange", e);
        }
        req.setAttribute("pageTitle", "Exchange #" + id);
        Views.render(req, resp, "exchange");
    }
}
