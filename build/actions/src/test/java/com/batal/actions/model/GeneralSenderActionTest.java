package com.batal.actions.model;

import com.batal.actions.model.interfaces.SimpleAction;
import com.batal.actions.model.messages.Message;
import io.opentracing.Span;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralSenderActionTest {

    @Test
    public void checkGeneralSenderActionWithSmallDelay() {
        int[] x = new int[1];
        GeneralSenderAction senderAction = new GeneralSenderAction(new MySimpleAction(x, 150));
        senderAction.setExecutor(getThreadPoolTaskExecutor());
        senderAction.setQueue(new ConcurrentLinkedQueue<>());
        senderAction.run(now().plus(ofSeconds(1)), 4);
        assertEquals(3, x[0]);
    }

    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(15);
        executor.setCorePoolSize(4);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("th-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(120);
        executor.initialize();
        return executor;
    }

    private static class MySimpleAction implements SimpleAction {
        private final int[] x;
        private final int timeout;

        public MySimpleAction(int[] x, int timeout) {
            this.x = x;
            this.timeout = timeout;
        }

        @Override
        public String getId() {
            return "1";
        }

        @Override
        public Message fetch(Span parentSpan) {
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (Exception e) {
            }
            return new Message("1", 1);
        }

        @Override
        public Message process(Message obj) {
            return obj;
        }

        @Override
        public void save(Span parentSpan, Message obj) {
            x[0]++;
        }

        @Override
        public void fix(Span parentSpan, Message obj, int code, String msg) {

        }
    }
}
