package com.campusloop.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Forwards to JSP views stored (unreachable directly) under /WEB-INF/views. */
public final class Views {

    private Views() {
    }

    /** Renders /WEB-INF/views/{name}.jsp, e.g. render(req, resp, "login"). */
    public static void render(HttpServletRequest req, HttpServletResponse resp, String name)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, resp);
    }
}
