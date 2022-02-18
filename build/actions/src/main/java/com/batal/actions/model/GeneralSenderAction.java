package com.batal.actions.model;

import com.batal.actions.model.interfaces.GeneralAction;
import com.batal.actions.model.interfaces.QueueSetter;
import com.batal.actions.model.interfaces.SimpleAction;
import com.batal.actions.model.interfaces.TaskExecutorSetter;
import com.batal.actions.model.messages.Message;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;

public class GeneralSenderAction implements GeneralAction, TaskExecutorSetter, QueueSetter {

    private final SimpleAction action;
    private ThreadPoolTaskExecutor executor;
    private Queue<Message> queue;

    public GeneralSenderAction(SimpleAction action) {
        this.action = action;
    }

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    public void setQueue(Queue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public String getId() {
        return action.getId();
    }

    public void run(LocalDateTime finishTime, int rate) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan(action.getId()).start();
        try {
            LocalDateTime currentTime = now();
            Duration runDuration = between(currentTime, finishTime).dividedBy(rate);
            int i = 0;
            int cnt = queue.size();
            for (int ii = 0; ii < rate - cnt; ii++) {
                executor.submit(() -> {
                    try {
                        Message message = action.fetch(span);
                        queue.add(action.process(message));
                    } catch (Exception e) {
                        action.fix(span, null, -1, e.getMessage());
                    }
                });
            }

            while (currentTime.isBefore(finishTime) && i < rate) {
                if (!queue.isEmpty()) {
                    Message message = queue.poll();
                    action.save(span, message);
                    action.fix(span, message, 2, "done");
                    i++;
                }
                Duration z = between(now(), currentTime.plus(runDuration));
                if (!z.isNegative()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(z.toMillis());
                    } catch (Exception ignored) {
                    }
                }
                currentTime = now();
            }
        } finally {
            span.finish();
        }
    }
}