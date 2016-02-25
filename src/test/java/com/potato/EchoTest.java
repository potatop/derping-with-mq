package com.potato;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.*;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class EchoTest {
    private static Connection connection;
    private ConcurrentHashMap<String, MessageHolder> dataMap = new ConcurrentHashMap<>();

    @BeforeClass
    public static void setUpBeforeClass() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);

        // create a Connection
        connection = connectionFactory.createConnection();
        connection.start();
        // create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue inquiryQueue = session.createQueue("com.potato.echo.inquiry");
        MessageConsumer consumer = session.createConsumer(inquiryQueue);
        consumer.setMessageListener(new Echo(session));
    }

    @AfterClass
    public static void tearDownAfterClass() throws JMSException {
        connection.close();
    }

    @Test
    public void testSendMessage() throws Exception {
        EchoProducer echoProducer = new EchoProducer(dataMap);
        echoProducer.start();

        Thread thread1 = new Thread(new Snake(echoProducer), "Thread-1");
        Thread thread2 = new Thread(new Snake(echoProducer), "Thread-2");
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        echoProducer.stop();

        System.out.print(dataMap);
    }
}