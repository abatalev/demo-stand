package com.batal.actions.model.interfaces;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface TaskExecutorSetter {
    void setExecutor(ThreadPoolTaskExecutor executor);
}
