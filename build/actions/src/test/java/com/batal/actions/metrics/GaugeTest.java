package com.batal.actions.metrics;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GaugeTest {

    @Test
    public void checkNano(){
        GaugeSet.Gauge gauge = new GaugeSet.Gauge( 5);
        LocalDateTime time = LocalDateTime.of(2021, 01, 01, 01, 01, 01);
        gauge.incWithTime(time);
        LocalDateTime time1 = time.plusNanos(100000);
        gauge.incWithTime(time1);
        LocalDateTime time2 = time.plusSeconds(1);
        gauge.incWithTime(time2);
        assertEquals(3, gauge.getWithTime(time2));
    }

    @Test
    public void checkMyGaugeInOneTime() {
        GaugeSet.Gauge gauge = new GaugeSet.Gauge( 5);
        LocalDateTime time = LocalDateTime.now();
        gauge.incWithTime(time);
        assertEquals(1, gauge.getWithTime(time));
        gauge.incWithTime(time);
        assertEquals(2, gauge.getWithTime(time));
        gauge.incWithTime(time);
        assertEquals(3, gauge.getWithTime(time));
        gauge.incWithTime(time);
        assertEquals(4, gauge.getWithTime(time));
    }

    @Test
    public void checkLongPeriod(){
        GaugeSet.Gauge gauge = new GaugeSet.Gauge( 5);
        LocalDateTime time = LocalDateTime.of(2021, 01, 01, 01, 01, 01);
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        time = time.plusSeconds(6);
        gauge.incWithTime(time);
        assertEquals(1, gauge.getWithTime(time));
    }

    @Test
    public void checkMyGaugeIn5Seconds() {
        GaugeSet.Gauge gauge = new GaugeSet.Gauge( 5);
        LocalDateTime time = LocalDateTime.of(2021, 01, 01, 01, 01, 01);
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(1, gauge.getWithTime(time));
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(2, gauge.getWithTime(time));
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(3, gauge.getWithTime(time));
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(4, gauge.getWithTime(time));
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(5, gauge.getWithTime(time));
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(5, gauge.getWithTime(time));
        time = time.plusSeconds(1);
        gauge.incWithTime(time);
        assertEquals(5, gauge.getWithTime(time));
    }
}
