package com.batal.actions.model.interfaces;

import com.batal.actions.model.messages.Message;
import io.opentracing.Span;

public interface Fixer {
    void fix(Span parentSpan, Message obj, int code, String msg);
}
