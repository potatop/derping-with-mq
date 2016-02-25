package com.potato;

import javax.jms.ObjectMessage;

public class MessageHolder {

    private ObjectMessage message = null;

    public ObjectMessage getMessage() throws InterruptedException {
        return message;
    }

    public void setMessage(ObjectMessage message) {
        this.message = message;
    }
}
