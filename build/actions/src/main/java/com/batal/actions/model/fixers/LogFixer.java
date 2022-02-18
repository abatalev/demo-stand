package com.batal.actions.model.fixers;

import com.batal.actions.model.messages.Message;
import com.batal.actions.model.interfaces.Fixer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFixer implements Fixer {
    private static final Logger log = LoggerFactory.getLogger(LogFixer.class);

    @Override
    public void fix(Span parentSpan, Message obj, int code, String msg) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("logfix").asChildOf(parentSpan).start();
        try {
            log.debug("fix {} {} {}", obj.getId(), code, msg);
        } finally {
            span.finish();
        }
    }
}
