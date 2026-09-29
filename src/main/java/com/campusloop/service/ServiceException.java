package com.campusloop.service;

/**
 * A business-rule failure whose message is safe and helpful to show to the user
 * (e.g. "This item has already been sold."). Never wraps SQL details.
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServiceException(String userMessage) {
        super(userMessage);
    }
}
