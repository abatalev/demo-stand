package com.batal.actions.model.fetchers;

import com.batal.actions.model.Message;
import com.batal.actions.model.interfaces.Fetcher;
import com.batal.actions.model.interfaces.JmsServiceSetter;
import com.batal.actions.services.JmsService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqFetcher implements Fetcher, JmsServiceSetter {
    private final static Logger log = LoggerFactory.getLogger(MqFetcher.class);

    private JmsService jmsService;

    private final String queueName;

    public MqFetcher(String queueName) {
        this.queueName = queueName;
    }

    public void setJmsService(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    public com.batal.actions.model.Message get(Span parentSpan) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("mqfetcher").asChildOf(parentSpan).start();
        span.setTag("queueName", queueName);
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try {
                    Message message = jmsService.getMessage(queueName);
                    if (message != null) {
                        log.debug("get " + message.getId());
                        span.setTag("result", "ok");
                        span.setTag("id", message.getId());
                        return message;
                    }

                    span.setTag("result", "empty");
                    return null;
                } catch (Throwable e) {
                    log.error("error " + e.getMessage());
                    span.setTag("result", "error," + e.getMessage());
                    return null;
                }
            }
        } finally {
            span.finish();
        }
    }
}
