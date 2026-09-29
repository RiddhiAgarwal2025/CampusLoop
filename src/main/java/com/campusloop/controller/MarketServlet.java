package com.campusloop.controller;

import com.campusloop.model.Category;
import com.campusloop.model.ItemCondition;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingFilter;
import com.campusloop.model.PagedResult;
import com.campusloop.service.MarketService;
import com.campusloop.util.Params;
import com.campusloop.util.Redirects;
import com.campusloop.util.Validator;
import com.campusloop.util.Views;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET /market  search + filters: q, category (slug), min, max, condition, sort, page.
 * Every parameter is validated; unknown or malformed values are ignored, never trusted.
 */
@WebServlet(name = "MarketServlet", urlPatterns = "/market")
public class MarketServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_QUERY_LENGTH = 80;

    private final MarketService marketService = new MarketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Category> categories = marketService.categories();
            ListingFilter filter = buildFilter(req, categories);
            PagedResult<Listing> result = marketService.search(filter);

            if (result.isEmpty() && filter.getPage() > 1 && result.getTotal() > 0) {
                String qs = filter.toQueryString(result.getTotalPages());
                Redirects.to(req, resp, "/market" + (qs.isEmpty() ? "" : "?" + qs));
                return;
            }

            Category selected = null;
            for (Category c : categories) {
                if (c.getSlug().equals(filter.getCategorySlug())) {
                    selected = c;
                }
            }
            req.setAttribute("categories", categories);
            req.setAttribute("selectedCategory", selected);
            req.setAttribute("filter", filter);
            req.setAttribute("result", result);
            req.setAttribute("conditions", ItemCondition.values());
        } catch (SQLException e) {
            throw new ServletException("Could not load the marketplace", e);
        }
        req.setAttribute("pageTitle", "Marketplace");
        req.setAttribute("activeNav", "browse");
        Views.render(req, resp, "market");
    }

    private ListingFilter buildFilter(HttpServletRequest req, List<Category> categories) {
        ListingFilter f = new ListingFilter();

        String q = Params.str(req, "q").replaceAll("\\s+", " ");
        if (q.length() > MAX_QUERY_LENGTH) {
            q = q.substring(0, MAX_QUERY_LENGTH).trim();
        }
        f.setQuery(q);

        String slug = Params.str(req, "category");
        for (Category c : categories) {
            if (c.getSlug().equals(slug)) {
                f.setCategorySlug(slug);
            }
        }

        BigDecimal min = Validator.parseMoney(req.getParameter("min"));
        BigDecimal max = Validator.parseMoney(req.getParameter("max"));
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal tmp = min;
            min = max;
            max = tmp;
        }
        f.setMinPrice(min);
        f.setMaxPrice(max);
        f.setCondition(ItemCondition.parse(req.getParameter("condition")));
        f.setSort(req.getParameter("sort"));
        f.setPage(Params.page(req));
        return f;
    }
}
