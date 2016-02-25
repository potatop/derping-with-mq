package com.potato;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EchoProducer implements MessageListener {

    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private Map<String, MessageHolder> map;

    public EchoProducer() {
        map = new HashMap<>();
    }

    public void start() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);

        // create a Connection
        connection = connectionFactory.createConnection();
        connection.start();
        // create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue replyQueue = session.createQueue("com.potato.echo.reply");
        MessageConsumer consumer = session.createConsumer(replyQueue);
        consumer.setMessageListener(this);

        // create the Queue to which messages will be sent
        Queue inquiryQueue = session.createQueue("com.potato.echo.inquiry");

        // create a MessageProducer for sending messages
        messageProducer = session.createProducer(inquiryQueue);
    }

    public void stop() throws JMSException {
        connection.close();
    }

    @Override
    public void onMessage(Message message) {
        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " onMessage");
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            if (map.containsKey(objectMessage.getJMSCorrelationID())) {
                MessageHolder holder = map.remove(objectMessage.getJMSCorrelationID());
                holder.setMessage(objectMessage);
                synchronized (holder) {
                    holder.notify();
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public ObjectMessage createMessage(String message) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage(message);
        String msgId = UUID.randomUUID().toString();
        objectMessage.setJMSCorrelationID(msgId);
        return objectMessage;
    }

    public void sendMessage(ObjectMessage objectMessage, MessageHolder holder) throws JMSException {
        System.out.println(Thread.currentThread().getName() + " sendMessage");
        map.put(objectMessage.getJMSCorrelationID(), holder);
        // send the message to the queue destination
        messageProducer.send(objectMessage);
    }
}
