package com.campusloop.service;

/** The requested record does not exist or is not visible to this student (rendered as HTTP 404). */
public class NotFoundException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String userMessage) {
        super(userMessage);
    }
}
