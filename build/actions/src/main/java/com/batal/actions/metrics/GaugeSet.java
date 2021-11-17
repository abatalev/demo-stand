package com.batal.actions.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.min;

public class GaugeSet {

    static class Gauge {

        private final AtomicInteger[] counters;
        private LocalDateTime prevTime = null;

        public Gauge(int periodInSeconds) {
            counters = new AtomicInteger[periodInSeconds];
            for (int i = 0; i < counters.length; i++) {
                counters[i] = new AtomicInteger(0);
            }
        }

        public void inc() {
            incWithTime(LocalDateTime.now());
        }

        public void incWithTime(LocalDateTime time) {
            swap(time);
            counters[0].incrementAndGet();
        }

        private void swap(LocalDateTime time) {
            LocalDateTime truncTime = time.truncatedTo(ChronoUnit.SECONDS);
            if (this.prevTime == null) {
                this.prevTime = truncTime;
                return;
            }
            int seconds = (int) Duration.between(this.prevTime, truncTime).getSeconds();
            if (seconds <= 0) {
                return;
            }
            int len = counters.length;
            if (seconds < len) {
                for (int i = 0; i < len - seconds; i++) {
                    counters[len - 1 - i] = counters[len - 1 - seconds - i];
                }
            }
            for (int i = 0; i < min(seconds, len); i++) {
                counters[i] = new AtomicInteger(0);
            }

            this.prevTime = truncTime;
        }

        public double get() {
            return (0. + getWithTime(LocalDateTime.now())) / counters.length;
        }

        public int getWithTime(LocalDateTime time) {
            swap(time);
            int value = Arrays.stream(counters).mapToInt(AtomicInteger::get).sum();
            // log.info("get {} {} - {}", name, time, value);
            return value;
        }
    }

    private static final Map<String, Gauge> gauges = new HashMap<>();

    private final String metricName;
    private final List<Tag> tags = new ArrayList<>();

    private MeterRegistry meterRegistry;

    public GaugeSet(String metricName, String... metricTags) {
        this.metricName = metricName;
        for (int i = 0; i < metricTags.length; i++) {
            if (i % 2 == 1) {
                tags.add(Tag.of(metricTags[i - 1], metricTags[i]));
            }
        }
    }

    public void setMetricRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void inc(String metricName) {
        if (meterRegistry == null) {
            return;
        }
        internalInc(metricName);
        internalInc("total");
    }

    private void internalInc(String statusName) {
        incGauge(statusName);
    }

    private void incGauge(String statusName) {
        String key = getKey(statusName);
        Gauge myGauge = gauges.get(key);
        if (myGauge != null) {
            myGauge.inc();
            return;
        }

        synchronized (this) {
            myGauge = gauges.get(key);
            if (myGauge == null) {
                myGauge = new Gauge(30);
                gauges.put(key, myGauge);
                io.micrometer.core.instrument.Gauge.builder(metricName, myGauge, Gauge::get)
                        .tags(getTags(statusName))
                        .strongReference(true)
                        .register(meterRegistry);
            }
        }
        myGauge.inc();
    }

    private String getKey(String statusName) {
        StringBuilder x = new StringBuilder();
        x.append(metricName);
        for (Tag tag : getTags(statusName)) {
            x.append("-").append(tag.getKey())
                    .append("-").append(tag.getValue());
        }
        return x.toString();
    }

    private List<Tag> getTags(String statusName) {
        List<Tag> myTags = new ArrayList<>(tags);
        myTags.add(Tag.of("status", statusName));
        return myTags;
    }
}
