package com.batal.actions.model.savers;

import com.batal.actions.model.messages.Message;
import com.batal.actions.model.interfaces.Saver;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class FileSaver implements Saver {

    private final File outDir;

    public FileSaver(String outDirName) {
        this.outDir = new File(outDirName);
        if (!this.outDir.exists()) {
            this.outDir.mkdir();
        }
    }

    @Override
    public void put(Span parentSpan, Message message) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("fileSaver").asChildOf(parentSpan).start();
        // TODO tags for FileSaver -- span.setTag("outDir",?);
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try (FileOutputStream stream = new FileOutputStream(new File(outDir, "1.txt"))) {
                    stream.write(getBytes(message));
                }
                span.setTag("result", "ok");
            } catch (Exception e) {
                span.setTag("error", "true");
                span.setTag("result", "error," + e.getMessage());
            }
        } finally {
            span.finish();
        }
    }

    private byte[] getBytes(Message message) {
        // TODO message.getPayload()
        return ("" + message.getPayload()).getBytes(StandardCharsets.UTF_8);
    }
}
