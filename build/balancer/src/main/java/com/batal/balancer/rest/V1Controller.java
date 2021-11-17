package com.batal.balancer.rest;

import com.batal.balancer.dto.v1.ActionConfig;
import com.batal.balancer.dto.v1.ActionsConfig;
import com.batal.balancer.dto.v1.BalancerData;
import com.batal.balancer.model.Group;
import com.batal.balancer.model.Instance;
import com.batal.balancer.model.Notify;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingExchangeFilterFunction;
import io.opentracing.contrib.spring.web.client.WebClientSpanDecorator;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.stream.Collectors.toMap;

@RestController
public class V1Controller {
    private static final Logger log = LoggerFactory.getLogger(V1Controller.class);
    private Map<String, Group> instances = new HashMap<>(); // TODO atomic
    private Queue<Notify> notifyQueue = new ConcurrentLinkedQueue<>();
    private ActionsConfig config = null;

    @Scheduled(fixedDelayString = "5000")
    public void reconfig() {
        log.info("reconfig");
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("reconfig").start();
        try {
            config = WebClient.builder() // TODO use loaded data
                    .filter(new TracingExchangeFilterFunction(tracer,
                            Collections.singletonList(
                                    new WebClientSpanDecorator.StandardTags())))
                    //.filter(logRequest())
                    .build().get()
                    .uri("http://configurer:8080/api/config/v1/balancer") // TODO use config data
                    .retrieve().bodyToMono(ActionsConfig.class).block();
        } finally {
            span.finish();
        }
    }

    @Scheduled(fixedDelayString = "1000")
    public void rebalancing() {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("rebalancing").start();
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            // log.info("> change notifies");
            while (!notifyQueue.isEmpty()) {
                Notify notify = notifyQueue.poll();
                // log.info("> notify " + notify.getHostname());
                Instance instance = getInstance(notify.getHostname(), notify.getAppType());
                instance.setLastAccess(notify.getTime());
            }
            Map<String, ActionConfig> map = getActionsMap();
            // log.info("> recalc rate");
            for (String key : instances.keySet()) {
                // log.info("> recalc rate for " + key);
                instances.get(key).remoteDeadAndRecalc(currentTime, map);
            }
        } finally {
            span.finish();
        }
    }

    @GetMapping(value = "/api/balancer/v1/actions/{id}", produces = "application/json")
    public BalancerData getActions(@PathVariable("id") String appName) {
        Tracer tracer = GlobalTracer.get();
        Span serverSpan = tracer.activeSpan();
        log.info("parent span - {}", serverSpan);
        Span span = tracer.buildSpan("getActions").asChildOf(serverSpan).start();
        try {
            span.setTag("app", appName);
            log.info("getActions " + appName);
            Instance instance = getInstance(appName, "actions");
            notify("actions", appName);
            return instance.getData();
        } finally {
            span.finish();
        }
    }

    private void notify(String appType, String hostname) {
        notifyQueue.add(new Notify(appType, hostname));
    }

    private Instance getInstance(String hostname, String appType) {
        Group group = instances.get(appType);
        if (group == null) {
            group = new Group();
            instances.put(appType, group);
        }
        Map<String, Instance> actions = group.getInstances();
        Instance instance = actions.get(hostname);
        if (instance == null) {
            instance = new Instance(hostname, appType);
            if (actions.size() == 0) {
                instance.setActive(true);
            }
            instance.rebalance(getActionsMap());
            actions.put(hostname, instance);
        }
        return instance;
    }

    private Map<String, ActionConfig> getActionsMap() {
        return config == null ? new HashMap<>()
                : config.getActions().stream().collect(toMap(ActionConfig::getId, action -> action, (a, b) -> b));
    }
}
