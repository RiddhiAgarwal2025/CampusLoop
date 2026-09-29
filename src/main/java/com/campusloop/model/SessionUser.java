package com.campusloop.model;

import com.campusloop.util.Format;

import java.io.Serializable;

/**
 * Minimal, non-sensitive identity kept in the HttpSession under "authUser".
 * Balances, points and ratings are always re-read from the database.
 */
public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String email;
    private final String profileImage;

    public SessionUser(int id, String name, String email, String profileImage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
    }

    public static SessionUser from(Student s) {
        return new SessionUser(s.getId(), s.getName(), s.getEmail(), s.getProfileImage());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getFirstName() {
        return Format.firstName(name);
    }
}
