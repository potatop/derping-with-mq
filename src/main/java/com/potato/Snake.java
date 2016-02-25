package com.potato;

import io.codearte.jfairy.Fairy;

import javax.jms.ObjectMessage;
import java.util.Map;

public class Snake implements Runnable {

    private EchoProducer echoProducer;
    private Map<String, String> inquiries;
    private Map<String, String> replies;

    public Snake(EchoProducer echoProducer, Map<String, String> inquiries, Map<String, String> replies) {
        this.echoProducer = echoProducer;
        this.inquiries = inquiries;
        this.replies = replies;
    }

    @Override
    public void run() {
        try {
            Fairy fairy = Fairy.create();
            String word = fairy.textProducer().word();
            ObjectMessage objectMessage = echoProducer.createMessage(word);
            inquiries.put(objectMessage.getJMSCorrelationID(), word);
            MessageHolder holder = new MessageHolder();
            echoProducer.sendMessage(objectMessage, holder);
            if (holder.getMessage() == null) {
                synchronized (holder) {
                    if (holder.getMessage() == null) {
                        System.out.println(Thread.currentThread().getName() + " wait");
                        holder.wait();
                    }
                }
            }
            replies.put(holder.getMessage().getJMSCorrelationID(), (String) holder.getMessage().getObject());
            System.out.println(Thread.currentThread().getName() + " ========================"
                    + holder.getMessage().getObject() + "========================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
