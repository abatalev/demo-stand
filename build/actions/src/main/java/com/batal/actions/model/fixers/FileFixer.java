package com.batal.actions.model.fixers;

import com.batal.actions.model.messages.FileMessage;
import com.batal.actions.model.messages.Message;
import com.batal.actions.model.interfaces.Fixer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import java.io.File;

import static java.nio.file.Paths.get;

public class FileFixer implements Fixer {
    private final File goodDir;
    private final File badDir;

    public FileFixer(String goodDirName, String badDirName) {
        this.goodDir = get(goodDirName).toFile();
        if (!goodDir.exists()) {
            goodDir.mkdir();
        }

        this.badDir = get(badDirName).toFile();
        if (!badDir.exists()) {
            badDir.mkdir();
        }
    }

    @Override
    public void fix(Span parentSpan, Message obj, int code, String msg) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("fileFixer").asChildOf(parentSpan).start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                if (!(obj instanceof FileMessage)) {
                    span.setTag("error", "true");
                    span.setTag("result", "error, move");
                    return;
                }

                File file = new File(((FileMessage) obj).getFilename());
                if (!file.exists()) {
                    span.setTag("error", "true");
                    span.setTag("result", "error, not exists");
                    return;
                }

                File dest = new File(code == 2 ? goodDir : badDir, file.getName());
                if (!file.renameTo(dest)) {
                    span.setTag("error", "true");
                    span.setTag("result", "error, move");
                    return;
                }

                span.setTag("result", "ok");
            }
        } finally {
            span.finish();
        }
    }
}
