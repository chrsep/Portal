package com.directdev.portal.tools.event;

public class UpdateErrorEvent {
    private String error;

    public String getError() {
        return error;
    }

    public UpdateErrorEvent(String error) {
        this.error = error;
    }
}
