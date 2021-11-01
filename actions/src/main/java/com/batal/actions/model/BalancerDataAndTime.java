package com.batal.actions.model;

import java.time.LocalDateTime;

import static java.time.Duration.between;

public class BalancerDataAndTime {
    public BalancerData balancerData;
    public LocalDateTime loadedTime;

    public boolean isOld(LocalDateTime currTime, int period) {
        return loadedTime != null && between(loadedTime, currTime).getSeconds() <= period;
    }
}
