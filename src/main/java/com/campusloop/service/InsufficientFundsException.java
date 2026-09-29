package com.campusloop.service;

/** Payment refused because the buyer's wallet does not cover the amount. Nothing was charged. */
public class InsufficientFundsException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public InsufficientFundsException(String userMessage) {
        super(userMessage);
    }
}
