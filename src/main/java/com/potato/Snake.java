package com.potato;

import io.codearte.jfairy.Fairy;

public class Snake implements Runnable {

    private EchoProducer echoProducer;
    MessageHolder holder = new MessageHolder();

    public Snake(EchoProducer echoProducer) {
        this.echoProducer = echoProducer;
    }

    @Override
    public void run() {
        try {
            Fairy fairy = Fairy.create();
            echoProducer.sendMessage(fairy.textProducer().word(), holder);
            if (holder.getMessage() == null) {
                synchronized (holder) {
                    if (holder.getMessage() == null) {
                        System.out.println(Thread.currentThread().getName() + "wait");
                        holder.wait();
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + "========================" + holder.getMessage() + "========================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
