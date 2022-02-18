package com.batal.actions.model.interfaces;

import com.batal.actions.model.messages.Message;

import java.util.Queue;

public interface QueueSetter {
    void setQueue(Queue<Message> o);
}
