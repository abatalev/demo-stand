package com.batal.actions.model.fetchers;

import com.batal.actions.model.messages.FileMessage;
import com.batal.actions.model.messages.Message;
import com.batal.actions.model.interfaces.Fetcher;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class FileFetcher implements Fetcher {

    private final Path inDir;
    private final File outDir;

    public FileFetcher(String in, String temp) {
        this.inDir = Paths.get(in);
        this.outDir = Paths.get(temp).toFile();
        if (!outDir.exists()) {
            outDir.mkdir();
        }
    }

    @Override
    public Message get(Span parentSpan) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan("fileFetcher").asChildOf(parentSpan).start();
        try {
            try (Scope ignored = tracer.activateSpan(span)) {
                try (Stream<Path> list = Files.list(inDir)) {
                    Optional<Path> path = list.filter(p -> p.toFile().isFile()).findFirst();
                    if (path.isPresent()) {
                        Path path1 = path.get();
                        File inFile = path1.toFile();
                        File outFile = new File(outDir, inFile.getName());
                        if (!inFile.renameTo(outFile)) {
                            throw new Exception("Cant move");
                        }

                        String id = UUID.randomUUID().toString();
                        FileMessage message = new FileMessage(id, outFile.getAbsolutePath());
                        span.setTag("result", "ok");
                        span.setTag("id", message.getId());
                        return message;
                    }
                    span.setTag("result", "empty");
                    return null;
                } catch (Exception e) {
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
