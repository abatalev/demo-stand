package com.batal.actions.model.actions;

import com.batal.actions.model.Message;
import com.batal.actions.model.interfaces.Fetcher;
import com.batal.actions.model.interfaces.Fixer;
import com.batal.actions.model.interfaces.Saver;
import com.batal.actions.model.interfaces.SimpleAction;
import io.opentracing.Span;

public class StandardAction implements SimpleAction {

    private final String id;

    private final Fetcher fetcher;
    private final Saver saver;
    private final Fixer fixer;

    public StandardAction(
            String id,
            Fetcher fetcher, Saver saver, Fixer fixer) {
        this.id = id;

        this.fetcher = fetcher;
        this.saver = saver;
        this.fixer = fixer;
    }

    public String getId() {
        return id;
    }

    @Override
    public Message fetch(Span parentSpan) {
        return fetcher.get(parentSpan);
    }

    @Override
    public Message process(Message obj) {
        return obj;
    }

    @Override
    public void save(Span parentSpan, Message obj) {
        saver.put(parentSpan,obj);
    }

    @Override
    public void fix(Span parentSpan, String msgId, int code, String msg) {
        fixer.fix(parentSpan, msgId, code, msg);
    }
}
