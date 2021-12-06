package com.batal.actions.model.loaders;

import com.batal.actions.model.ActionsConfigAndTime;
import com.batal.actions.model.config.ActionsConfig;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Consumer;

@Component
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    private final String configUrl;

    @Autowired
    public ConfigLoader(
            @Value("${actions.config.url}") String configUrl
    ) {
        this.configUrl = configUrl;
    }

    private String getConfigUrl() {
        return configUrl;
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    @Async("configPool")
    public ActionsConfigAndTime reload(LocalDateTime currTime, Consumer<ActionsConfigAndTime> consumer) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("config").start();
        try {
            try {
                ActionsConfig config = WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(
                                HttpClient.create().wiretap(true)))
                        .filter(new TracingExchangeFilterFunction(tracer,
                                Collections.singletonList(
                                        new WebClientSpanDecorator.StandardTags())))
                        .filter(logRequest())
                        .build().get()
                        .uri(getConfigUrl())
                        .retrieve().bodyToMono(ActionsConfig.class).block();
                if (config == null) {
                    span.setTag("result", "empty");
                    return null;
                }
                span.setTag("result", "ok");
                ActionsConfigAndTime newData = new ActionsConfigAndTime();
                newData.config = config;
                newData.loadedTime = currTime;
                consumer.accept(newData);
                return newData;
            } catch (Throwable e) {
                span.setTag("error", "true");
                span.setTag("result", "error," + e.getMessage());
                return null;
            }
        } finally {
            span.finish();
        }
    }
}
