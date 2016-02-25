package com.potato;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EchoTest {
    private static Connection connection;

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
        EchoProducer echoProducer = new EchoProducer();
        echoProducer.start();
        Map<String, String> inquires = new HashMap<>();
        Map<String, String> replies = new HashMap<>();

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Thread thread = new Thread(new Snake(echoProducer, inquires, replies), "Thread-" + (i + 1));
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads)
        {
            thread.join();
        }
        for (String id : inquires.keySet())
        {
            assertThat(inquires.get(id), is(new StringBuilder(replies.get(id)).reverse().toString()));
        }

    }
}