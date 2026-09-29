package com.campusloop.service;

/** The record exists but belongs to another student (rendered as HTTP 403). */
public class ForbiddenException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(String userMessage) {
        super(userMessage);
    }
}
