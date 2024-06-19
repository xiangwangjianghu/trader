package com.newtouch.configs;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean(name = "asyncGatewayExecutor")
    public ThreadPoolTaskExecutor getAsyncGatewayExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("asyncGatewayExecutor");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        executor.setTaskDecorator(runnable -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                DefaultTransactionAttribute def = new DefaultTransactionAttribute();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionStatus status = platformTransactionManager.getTransaction(def);

                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                try {
                    runnable.run();
                    if (runnable instanceof Future<?>) {

                        ((Future<?>) runnable).get();

                        platformTransactionManager.commit(status);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    platformTransactionManager.rollback(status);
                } finally {
                    MDC.clear();
                }
            };
        });

        return executor;
    }
}
