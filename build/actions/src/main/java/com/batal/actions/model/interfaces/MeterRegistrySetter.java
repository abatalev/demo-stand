package com.batal.actions.model.interfaces;

import io.micrometer.core.instrument.MeterRegistry;

public interface MeterRegistrySetter {
    void setMetricRegistry(MeterRegistry meterRegistry);
}
