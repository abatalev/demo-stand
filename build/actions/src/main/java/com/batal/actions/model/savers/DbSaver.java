package com.batal.actions.model.savers;

import com.batal.actions.model.messages.Message;
import com.batal.actions.model.interfaces.DbServiceSetter;
import com.batal.actions.model.interfaces.Saver;
import com.batal.actions.services.DbService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSaver implements Saver, DbServiceSetter {

    private final Logger log = LoggerFactory.getLogger(DbSaver.class);

    private DbService dbService;

    private final String tableName;

    public DbSaver(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

    public void put(Span parentSpan, Message message) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("dbsaver").asChildOf(parentSpan).start();
        span.setTag("tableName", tableName);
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                if (message == null) {
                    span.setTag("result", "empty");
                    return;
                }
                try {
                    dbService.put(tableName, message);
                    log.debug("put {} ok", message.getId());
                    span.setTag("result", "ok");
                } catch (RuntimeException e) {
                    log.error("error: {} {}", message.getId(), e.getMessage());
                    span.setTag("error", "true");
                    span.setTag("result", "error," + e.getMessage());
                }
            }
        } finally {
            span.finish();
        }
    }
}
