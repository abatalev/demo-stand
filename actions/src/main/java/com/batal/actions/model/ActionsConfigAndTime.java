package com.batal.actions.model;

import com.batal.actions.model.config.ActionsConfig;

import java.time.LocalDateTime;

import static java.time.Duration.between;

public class ActionsConfigAndTime {
    public ActionsConfig config = new ActionsConfig();
    public LocalDateTime loadedTime;

    public boolean isOld(LocalDateTime currTime, int period) {
        return loadedTime != null && between(loadedTime, currTime).getSeconds() <= period;
    }
}
