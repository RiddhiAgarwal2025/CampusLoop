package com.campusloop.controller;

import com.campusloop.model.SessionUser;
import com.campusloop.model.Student;
import com.campusloop.service.ProfileService;
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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * GET  /profile  the signed-in student's own profile (no id parameter exists)
 * POST /profile  action=save (name + optional photo, multipart) | action=removePhoto
 */
@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024)
public class ProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProfileService profileService = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int me = SessionUtil.requireUserId(req);
        String name = Params.str(req, "name");
        try {
            Student updated;
            if ("removePhoto".equals(req.getParameter("action"))) {
                updated = profileService.removePhoto(me);
                SessionUtil.flashSuccess(req, "Your profile photo was removed.");
            } else {
                updated = profileService.update(me, name, photoPart(req));
                SessionUtil.flashSuccess(req, "Your profile has been updated.");
            }
            SessionUtil.updateAuthUser(req, SessionUser.from(updated));
            Redirects.to(req, resp, "/profile");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            render(req, resp, name);
        } catch (ServiceException e) {
            SessionUtil.flashError(req, e.getMessage());
            Redirects.to(req, resp, "/profile");
        } catch (SQLException e) {
            throw new ServletException("Could not update the profile", e);
        }
    }

    private static Part photoPart(HttpServletRequest req) throws IOException {
        try {
            return req.getPart("photo");
        } catch (ServletException | IllegalStateException e) {
            return null;
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, String typedName)
            throws ServletException, IOException {
        try {
            req.setAttribute("profile", profileService.view(SessionUtil.requireUserId(req)));
        } catch (ServiceException | SQLException e) {
            throw new ServletException("Could not load the profile", e);
        }
        if (req.getAttribute("errors") == null) {
            req.setAttribute("errors", Collections.emptyMap());
        }
        req.setAttribute("typedName", typedName);
        req.setAttribute("pageTitle", "Profile");
        Views.render(req, resp, "profile");
    }
}
