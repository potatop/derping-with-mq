package com.potato;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Map;
import java.util.UUID;

public class EchoProducer implements MessageListener {

    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    Map<String, MessageHolder> map;

    public EchoProducer(Map<String, MessageHolder> map) {
        this.map = map;
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
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " oh hi!");
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            String msg = (String) objectMessage.getObject();
            if (map.containsKey(message.getJMSCorrelationID())) {
                MessageHolder holder = map.remove(message.getJMSCorrelationID());
                holder.setMessage(msg);
                synchronized (holder) {
                    holder.notify();
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, MessageHolder holder) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage(message);
        String correlationId = UUID.randomUUID().toString();
        System.out.println(Thread.currentThread().getName() + " : " + message);
        objectMessage.setJMSCorrelationID(correlationId);
        map.put(correlationId, holder);
        // send the message to the queue destination
        messageProducer.send(objectMessage);
    }
}
