package com.batal.actions.model.loaders;

import com.batal.actions.model.BalancerData;
import com.batal.actions.model.BalancerDataAndTime;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingExchangeFilterFunction;
import io.opentracing.contrib.spring.web.client.WebClientSpanDecorator;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class BalancerLoader {
    private static final Logger log = LoggerFactory.getLogger(BalancerLoader.class);

    private final String hostname;
    private final String balancerUrl;

    @Autowired
    public BalancerLoader(
            @Value("${actions.balancer.url}") String balancerUrl
    ) {
        this.balancerUrl = balancerUrl;
        this.hostname = getHostName();
        log.info("hostname " + hostname);
    }

    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return UUID.randomUUID().toString();
        }
    }

    @Async("balancerPool")
    public BalancerDataAndTime reload(LocalDateTime currTime, Consumer<BalancerDataAndTime> consumer) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("balance-reload").ignoreActiveSpan().start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try {
                    BalancerData newBalancerData = load(span);
                    if (newBalancerData == null) {
                        return null;
                    }

                    BalancerDataAndTime data = new BalancerDataAndTime();
                    data.balancerData = newBalancerData;
                    data.loadedTime = currTime;
                    consumer.accept(data);
                    return data;
                } catch (Throwable e) {
                    span.setTag("result", "error," + e.getMessage());
                    return null;
                }
            }
        } finally {
            span.finish();
        }
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    private BalancerData load(Span parentSpan) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("balance-load").asChildOf(parentSpan).start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try {
                    return WebClient.builder()
                            .clientConnector(new ReactorClientHttpConnector(
                                    HttpClient.create().wiretap(true)))
                            .filter(new TracingExchangeFilterFunction(tracer,
                                    Collections.singletonList(
                                            new WebClientSpanDecorator.StandardTags())))
                            .filter(logRequest())
                            .build().get()
                            .uri(getBalancerUrl())
                            .retrieve().bodyToMono(BalancerData.class).block();
                } catch (RuntimeException e) {
                    log.error(e.getMessage());
                    return null;
                }
            }
        } finally {
            span.finish();
        }
    }

    private String getBalancerUrl() {
        StringJoiner joiner = new StringJoiner("/");
        joiner.add(balancerUrl).add(hostname);
        return joiner.toString();
    }
}
