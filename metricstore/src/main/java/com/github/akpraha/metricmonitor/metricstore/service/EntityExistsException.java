package com.github.akpraha.metricmonitor.metricstore.service;

/**
 * @author Andy Key
 * @created 12/28/2024, Sat
 */
public class EntityExistsException extends RuntimeException {

    public EntityExistsException() {
    }

    public EntityExistsException(String message) {
        super(message);
    }
}
