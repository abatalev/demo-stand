package com.batal.actions.model.fetchers;

import com.batal.actions.metrics.GaugeSet;
import com.batal.actions.model.Message;
import com.batal.actions.model.interfaces.Fetcher;
import com.batal.actions.model.interfaces.JmsServiceSetter;
import com.batal.actions.model.interfaces.MeterRegistrySetter;
import com.batal.actions.services.JmsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqFetcher implements Fetcher, JmsServiceSetter, MeterRegistrySetter {
    private final static Logger log = LoggerFactory.getLogger(MqFetcher.class);

    private JmsService jmsService;

    private final String queueName;
    private final GaugeSet gaugeSet;

    public MqFetcher(String queueName) {
        this.queueName = queueName;
        this.gaugeSet = new GaugeSet("mqFetcher", "queue", queueName);
    }

    public void setJmsService(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    @Override
    public void setMetricRegistry(MeterRegistry meterRegistry) {
        this.gaugeSet.setMetricRegistry(meterRegistry);
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
                        gaugeSet.inc("good");
                        return message;
                    }

                    span.setTag("result", "empty");
                    gaugeSet.inc("empty");
                    return null;
                } catch (Throwable e) {
                    log.error("error " + e.getMessage());
                    span.setTag("error", "true");
                    span.setTag("result", "error," + e.getMessage());
                    gaugeSet.inc("error");
                    return null;
                }
            }
        } finally {
            span.finish();
        }
    }
}
