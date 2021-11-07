package com.batal.actions.model.interfaces;

import com.batal.actions.model.Message;
import io.opentracing.Span;

public interface SimpleAction {
    String getId();

    Message fetch(Span parentSpan);

    Message process(Message obj);

    void save(Span parentSpan,Message obj);

    void fix(Span parentSpan, String msgId, int code, String msg);
}
