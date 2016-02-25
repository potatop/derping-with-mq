package com.potato;

public class MessageHolder {

    private String message = null;

    public String getMessage() throws InterruptedException {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
