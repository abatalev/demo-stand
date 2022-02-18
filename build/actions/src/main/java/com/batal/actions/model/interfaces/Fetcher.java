package com.batal.actions.model.interfaces;

import com.batal.actions.model.messages.Message;
import io.opentracing.Span;

public interface Fetcher {
    Message get(Span parentSpan);
}
