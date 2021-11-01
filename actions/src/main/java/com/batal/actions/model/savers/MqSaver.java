package com.batal.actions.model.savers;

import com.batal.actions.model.Message;
import com.batal.actions.model.interfaces.JmsServiceSetter;
import com.batal.actions.model.interfaces.Saver;
import com.batal.actions.services.JmsService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqSaver implements Saver, JmsServiceSetter {

    private static final Logger log = LoggerFactory.getLogger(MqSaver.class);

    private JmsService jmsService;
    private final String queueName;

    public MqSaver(String queueName) {
        this.queueName = queueName;
    }

    public void setJmsService(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    public void put(Span parentSpan, Message message) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("mqsaver").asChildOf(parentSpan).start();
        span.setTag("queueName",queueName);
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                jmsService.put(queueName, message);
                log.debug("ok " + message.getId());
                span.setTag("result", "ok");
            }
        } finally {
            span.finish();
        }
    }
}
