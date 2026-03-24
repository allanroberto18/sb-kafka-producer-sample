package br.com.alr.api.sbkafkaproducersample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class AsyncConfiguration {

    @Bean(name = "taskExecutor")
    AsyncTaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("order-async-");
        executor.setVirtualThreads(true);
        return executor;
    }
}
