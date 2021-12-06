package com.batal.actions.model.fetchers;

import com.batal.actions.model.Message;
import com.batal.actions.model.interfaces.Fetcher;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

public class RestFetcher implements Fetcher {
    private static final Logger log = LoggerFactory.getLogger(RestFetcher.class);

    private final String url;

    public RestFetcher(String url) {
        this.url = url;
    }

    @Override
    public Message get(Span parentSpan) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("restfetcher").asChildOf(parentSpan).start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try {
                    span.setTag("url", url);
                    String payload = WebClient.builder().build().get().uri(url)
                            .retrieve().bodyToMono(String.class)
                            //.timeout(Duration.ofMillis(2000))
                            .block();
                    if (payload == null) {
                        span.setTag("result", "empty");
                        return null;
                    }
                    String id = UUID.randomUUID().toString();
                    log.debug("get {}", id);
                    span.setTag("result", "ok");
                    span.setTag("id", id);
                    return new Message(id, payload);
                } catch (Exception e) {
                    span.setTag("error", "true");
                    span.setTag("result", "error:" + e.getMessage());
                    log.error("error {}", e.getMessage());
                    return null; // TODO throws MessageException
                }
            }
        } finally {
            span.finish();
        }
    }
}
