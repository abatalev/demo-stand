package com.batal.actions.metrics;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GaugeTest {

    @Test
    public void checkNano() {
        GaugeSet.Gauge gauge = new GaugeSet.Gauge(5);
        LocalDateTime time = LocalDateTime.of(2021, 01, 01, 01, 01, 01);
        gauge.incWithTime(time);
        gauge.incWithTime(time.plusNanos(100000));
        incAndAssert(gauge, time.plusSeconds(1), 3);
    }

    @Test
    public void checkMyGaugeInOneTime() {
        GaugeSet.Gauge gauge = new GaugeSet.Gauge(5);
        LocalDateTime time = LocalDateTime.now();
        incAndAssert(gauge, time, 1);
        incAndAssert(gauge, time, 2);
        incAndAssert(gauge, time, 3);
        incAndAssert(gauge, time, 4);
    }

    @Test
    public void checkLongPeriod() {
        GaugeSet.Gauge gauge = new GaugeSet.Gauge(5);
        LocalDateTime time = LocalDateTime.of(2021, 01, 01, 01, 01, 01);
        time = step(gauge, time, 1, 1);
        step(gauge, time, 1, 6);
    }

    @Test
    public void checkMyGaugeIn5Seconds() {
        GaugeSet.Gauge gauge = new GaugeSet.Gauge(5);
        LocalDateTime time = LocalDateTime.of(2021, 01, 01, 01, 01, 01);
        time = step(gauge, time, 1, 1);
        time = step(gauge, time, 2, 1);
        time = step(gauge, time, 3, 1);
        time = step(gauge, time, 4, 1);
        time = step(gauge, time, 5, 1);
        time = step(gauge, time, 5, 1);
        step(gauge, time, 5, 1);
    }

    private LocalDateTime step(GaugeSet.Gauge gauge, LocalDateTime time, int expected, int cnt) {
        time = time.plusSeconds(cnt);
        incAndAssert(gauge, time, expected);
        return time;
    }

    private void incAndAssert(GaugeSet.Gauge gauge, LocalDateTime time, int expected) {
        gauge.incWithTime(time);
        assertEquals(expected, gauge.getWithTime(time));
    }
}
