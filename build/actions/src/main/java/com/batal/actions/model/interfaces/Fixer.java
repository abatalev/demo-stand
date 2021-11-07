package com.batal.actions.model.interfaces;

import io.opentracing.Span;

public interface Fixer {
    void fix(Span parentSpan, String msgId, int code, String msg);
}
