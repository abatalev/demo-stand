package com.batal.actions.model;

import com.batal.actions.model.interfaces.GeneralAction;
import com.batal.actions.model.interfaces.SimpleAction;
import com.batal.actions.model.interfaces.TaskExecutorSetter;
import com.batal.actions.model.messages.Message;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;

public class GeneralRetrieveAction implements GeneralAction, TaskExecutorSetter {

    private static final Logger log = LoggerFactory.getLogger(GeneralRetrieveAction.class);

    private ThreadPoolTaskExecutor executor;

    private final SimpleAction action;

    public GeneralRetrieveAction(SimpleAction action) {
        this.action = action;
    }

    public String getId() {
        return action.getId();
    }

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    public void run(LocalDateTime finishTime, int rate) {
        log.debug("start {}", action.getId());
        LocalDateTime currentTime = now();
        Duration runDuration = between(currentTime, finishTime).dividedBy(rate);
        int i = 0;
        while (currentTime.isBefore(finishTime) && i <= rate) {
            log.debug("step submit {} {}", action.getId(), currentTime);
            executor.submit(() -> {
                Tracer tracer = GlobalTracer.get();
                Span span = tracer.buildSpan("step_" + action.getId()).ignoreActiveSpan().start();
                try {
                    log.debug("step start {}", action.getId());
                    try {
                        Message message = action.fetch(span);
                        if (message != null) {
                            try {
                                action.save(span, action.process(message));
                                action.fix(span, message, 2, "done");
                            } catch (Throwable e) {
                                action.fix(span, message, -1, e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    } catch (Throwable e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                    log.debug("step finished {}", action.getId());
                } finally {
                    span.finish();
                }
            });
            Duration z = between(now(), currentTime.plus(runDuration));
            if (!z.isNegative()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(z.toMillis());
                } catch (Exception ignored) {
                }
            }
            i++;
            currentTime = now();
        }
        log.debug("finished {}", action.getId());
    }
}
