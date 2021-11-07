package com.batal.actions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// TODO openapi
@SpringBootApplication
@EnableAsync
@EnableJms
@EnableTransactionManagement
public class ActionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActionsApplication.class);
    }

    @Bean(name = "balancerPool")
    public ThreadPoolTaskExecutor balancerPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("bal-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "configPool")
    public ThreadPoolTaskExecutor configPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("cfg-");
        executor.initialize();
        return executor;
    }


    @Bean("pool")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("pool-");
        executor.initialize();
        return executor;
    }
}
