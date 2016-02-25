package com.potato;

import javax.jms.*;

public class Echo implements MessageListener {

    private Session session;
    private MessageProducer producer;

    public Echo(Session session) throws JMSException {
        this.session = session;
        Queue replyQueue = session.createQueue("com.potato.echo.reply");
        producer = session.createProducer(replyQueue);
    }

        @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            objectMessage = session.createObjectMessage(
                    new StringBuilder((String) objectMessage.getObject()).reverse().toString());
            objectMessage.setJMSCorrelationID(message.getJMSCorrelationID());
            producer.send(objectMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
