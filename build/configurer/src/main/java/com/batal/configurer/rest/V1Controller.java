package com.batal.configurer.rest;

import com.batal.configurer.dtos.v1.ActionsConfig;
import com.batal.configurer.dtos.v1.balance.BalanceConfig;
import io.micrometer.core.annotation.Timed;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Timed
public class V1Controller {
    private static final Logger log = LoggerFactory.getLogger(V1Controller.class);
    private final BalancerMapper balancerMapper;
    private final ActionMapper actionMapper;

    @Autowired
    public V1Controller(BalancerMapper balancerMapper, ActionMapper actionMapper) {
        this.balancerMapper = balancerMapper;
        this.actionMapper = actionMapper;
    }

    @GetMapping(value = "/api/config/v1/balancer", produces = "application/json")
    public BalanceConfig getBalancer() {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("getBalancer").asChildOf(tracer.activeSpan()).start();
        try {
            return balancerMapper.getConfig();
        } finally {
            span.finish();
        }
    }

    @GetMapping(value = "/api/config/v1/actions", produces = "application/json")
    public ActionsConfig getActions() {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("getActions").asChildOf(tracer.activeSpan()).start();
        try {
            return actionMapper.getConfig();
        } finally {
            span.finish();
        }
    }
}
