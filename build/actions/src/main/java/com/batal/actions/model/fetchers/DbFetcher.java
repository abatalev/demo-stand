package com.batal.actions.model.fetchers;

import com.batal.actions.model.Message;
import com.batal.actions.model.interfaces.DbServiceSetter;
import com.batal.actions.model.interfaces.Fetcher;
import com.batal.actions.model.savers.DbSaver;
import com.batal.actions.services.DbService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbFetcher implements Fetcher, DbServiceSetter {

    private final Logger log = LoggerFactory.getLogger(DbSaver.class);

    private DbService dbService;
    private final String tableName;

    public DbFetcher(String tableName) {
        this.tableName = tableName;
    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

    public Message get(Span parentSpan) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("dbfetcher").asChildOf(parentSpan).start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try {
                    Message message = dbService.getMessage(tableName);
                    if (message != null) {
                        span.setTag("result", "ok");
                        span.setTag("id", message.getId());
                        log.debug("get {}", message.getId());
                        return message;
                    }
                    span.setTag("result", "empty");
                    return null;
                } catch (RuntimeException e) {
                    log.error("error: " + e.getMessage());
                    span.setTag("error", "true");
                    span.setTag("result", "error," + e.getMessage());
                    return null; // TODO throws MessageException
                }
            }
        } finally {
            span.finish();
        }
    }
}
