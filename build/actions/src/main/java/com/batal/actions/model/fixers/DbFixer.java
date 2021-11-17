package com.batal.actions.model.fixers;

import com.batal.actions.model.interfaces.DbServiceSetter;
import com.batal.actions.model.interfaces.Fixer;
import com.batal.actions.services.DbService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbFixer implements Fixer, DbServiceSetter {
    private static final Logger log = LoggerFactory.getLogger(DbFixer.class);

    private final String tableName;
    private DbService dbService;

    public DbFixer(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void fix(Span parentSpan, String msgId, int code, String msg) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("dbfix").asChildOf(parentSpan).start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try {
                    dbService.fix(tableName, msgId, code, msg);
                    span.setTag("result", "ok");
                    log.debug("fix {} {} {}", msgId, code, msg);
                } catch (Exception e) {
                    span.setTag("result", "error," + e.getMessage());
                    log.error("error {} {}", msgId, e.getMessage());
                }
            }
        } finally {
            span.finish();
        }
    }
}
