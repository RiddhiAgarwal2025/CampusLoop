package com.campusloop.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Form validation failure: a map of field name -> message, rendered next to
 * each input (JSP: ${errors.title}), plus a summary message for the page.
 */
public class ValidationException extends ServiceException {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Please fix the highlighted fields and try again.");
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public ValidationException(String field, String message) {
        this(Collections.singletonMap(field, message));
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    /** Throws if {@code errors} is non-empty. */
    public static void throwIfAny(Map<String, String> errors) throws ValidationException {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
