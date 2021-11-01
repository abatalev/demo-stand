package com.batal.configurer.rest;

import com.batal.configurer.dtos.v1.*;
import io.micrometer.core.annotation.Timed;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Arrays.asList;

@RestController
@Timed
public class V1Controller {
    private static final Logger log = LoggerFactory.getLogger(V1Controller.class);

    @GetMapping(value = "/api/config/v1/balancer", produces = "application/json")
    public String getBalancer() {
        log.info("getBalancer");
        Tracer tracer = GlobalTracer.get();
        Span serverSpan = tracer.activeSpan();
        log.info("parent span - {}", serverSpan);
        Span span = tracer.buildSpan("getBalancer").asChildOf(serverSpan).start();
        try {
            return "{}";
        } finally {
            span.finish();
        }
    }

    @GetMapping(value = "/api/config/v1/actions", produces = "application/json")
    public ActionsConfig getActions() {
        log.info("getActions");
        Tracer tracer = GlobalTracer.get();
        Span serverSpan = tracer.activeSpan();
        log.info("parent span - {}", serverSpan);
        Span span = tracer.buildSpan("getActions").asChildOf(serverSpan).start();
        try {
            ActionsConfig config = new ActionsConfig();
            config.setEnabled(true);
            config.setBalancerPollingPeriod(5);
            config.setActions(asList( // TODO список настроек
                    new ActionConfig("mqdb", "a1",
                            new MqDbConfig("TBL1", "DEV.QUEUE.1")),
                    new ActionConfig("dbmq", "a2",
                            new MqDbConfig("TBL1", "DEV.QUEUE.1")),

                    new ActionConfig("custom", "yaru",
                            new CustomConfig(
                                    new RestConfig("http://ya.ru"),
                                    new MqConfig("DEV.QUEUE.1"),
                                    new StdConfig()
                            )),

                    new ActionConfig("custom", "googlecom",
                            new CustomConfig(
                                    new RestConfig("http://google.com"),
                                    new DbConfig("TBL1"),
                                    new StdConfig()
                            ))));
            return config;
        } finally {
            span.finish();
        }
    }
}
