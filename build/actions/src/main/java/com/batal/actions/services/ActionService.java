package com.batal.actions.services;

import com.batal.actions.model.ActionsConfigAndTime;
import com.batal.actions.model.BalancerData;
import com.batal.actions.model.BalancerDataAndTime;
import com.batal.actions.model.messages.Message;
import com.batal.actions.model.interfaces.GeneralAction;
import com.batal.actions.model.interfaces.QueueSetter;
import com.batal.actions.model.interfaces.TaskExecutorSetter;
import com.batal.actions.model.loaders.BalancerLoader;
import com.batal.actions.model.loaders.ConfigLoader;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import static java.time.LocalDateTime.now;

@Service
public class ActionService {
    private static final Logger log = LoggerFactory.getLogger(ActionService.class);

    private final ThreadPoolTaskExecutor executor;
    private final Map<String, Queue<Message>> queueMap = new HashMap<>();
    private final int configPoolingPeriod;
    private final BalancerLoader balancerLoader;
    private final ConfigLoader configLoader;
    private final ActionParser actionParser;

    private final AtomicReference<ActionsConfigAndTime> config;
    private final AtomicReference<BalancerDataAndTime> balancer;
    private final AtomicReference<List<GeneralAction>> actions;
    private Counter myConfigCounter;

    @Autowired
    public ActionService(
            MeterRegistry meterRegistry,
            ActionParser actionParser,
            @Qualifier("pool") ThreadPoolTaskExecutor executor,
            @Value("${actions.config.poolingPeriod}") int configPoolingPeriod,
            BalancerLoader balancerLoader,
            ConfigLoader configLoader
    ) {
        try {
            this.myConfigCounter = meterRegistry.counter("myConfigCounter", "mytag", "myvalue");
        } catch (Throwable e) {
            log.error("??? {}", e.getMessage());
            this.myConfigCounter = null;
        }
        this.balancer = new AtomicReference<>(new BalancerDataAndTime());
        this.actions = new AtomicReference<>();
        this.config = new AtomicReference<>(new ActionsConfigAndTime());
        this.actionParser = actionParser;
        this.executor = executor;
        this.configPoolingPeriod = configPoolingPeriod;
        this.balancerLoader = balancerLoader;
        this.configLoader = configLoader;
    }

    public void runActions() {
        LocalDateTime currTime = now();

        reloadConfig(currTime);

        if (!config.get().config.getEnabled()) {
            log.info("config disabled");
            return;
        }

        if (actions.get() == null) {
            log.info("actions not found");
            return;
        }

        reloadBalancer(currTime);

        log.info("Actions  " + currTime);
        for (GeneralAction action : actions.get()) {
            launchAction(currTime, action);
        }

        log.info("!!! stat {} {} {}", executor.getActiveCount(),
                executor.getMaxPoolSize(),
                executor.getThreadPoolExecutor().getQueue().size());
    }

    private void launchAction(LocalDateTime startTime, GeneralAction action) {
        BalancerData balancerData = balancer.get().balancerData;
        if (balancerData == null) {
            log.info("Skip 1    " + startTime + " " + action.getId());
            return;
        }

        Integer rate = balancerData.getRates().get(action.getId());
        if (rate == null || rate <= 0) {
            log.info("Skip 2    " + startTime + " " + action.getId());
            return;
        }

        if (action instanceof QueueSetter) {
            Queue<Message> queue = queueMap.get(action.getId());
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
                queueMap.put(action.getId(), queue);
            }
            ((QueueSetter) action).setQueue(queue);
        }

        if (action instanceof TaskExecutorSetter) {
            ((TaskExecutorSetter) action).setExecutor(executor);
        }

        log.debug("submit {}", action.getId());
        executor.submit(() -> {
            Tracer tracer = GlobalTracer.get();
            Span span = tracer.buildSpan(action.getId()).ignoreActiveSpan().start();
            try {
                try (Scope ignored = tracer.activateSpan(span)) {
                    try {
                        log.debug("Launch   {} {}", startTime, action.getId());
                        LocalDateTime finishTime = startTime.plus(Duration.ofSeconds(1));
                        action.run(finishTime, rate);
                        log.debug("Finished {} {}", startTime, action.getId());
                    } catch (Throwable e) {
                        log.error("Error   {} {} {}", startTime, action.getId(), e.getMessage());
                        e.printStackTrace();
                    }
                }
            } finally {
                span.finish();
            }
        });
    }

    private void reloadBalancer(LocalDateTime currTime) {
        if (balancer.get().isOld(currTime, config.get().config.getBalancerPollingPeriod())) {
            return;
        }

        log.info("balancer submit {}", currTime);
        balancerLoader.reload(currTime, data -> {
            log.info("balancer start {}", currTime);
            balancer.set(data);
            log.info("balancer finish {}", currTime);
        });
    }

    private void reloadConfig(LocalDateTime currTime) {
        myConfigCounter.increment();
        if (config.get().isOld(currTime, configPoolingPeriod)) {
            return;
        }

        log.info("config submit {}", currTime);
        configLoader.reload(currTime, data -> {
            log.info("config start {}", currTime);
            try {
                config.set(data);
                List<GeneralAction> tmpActions = actionParser.parse(config.get().config.getActions());
                actions.set(tmpActions);
                if (!data.config.getEnabled()) {
                    log.info("run actions disabled");
                }
                log.info("config finished {}", currTime);
            } catch (RuntimeException e) {
                log.info("config error {} {}", currTime, e.getMessage());
            }
        });
    }
}
