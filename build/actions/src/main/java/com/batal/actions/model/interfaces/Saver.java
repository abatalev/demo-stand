package com.batal.actions.model.interfaces;

import com.batal.actions.model.messages.Message;
import io.opentracing.Span;

public interface Saver {
    void put(Span parentSpan, Message obj);
}
