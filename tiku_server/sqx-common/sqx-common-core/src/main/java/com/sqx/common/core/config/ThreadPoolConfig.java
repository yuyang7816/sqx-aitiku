package com.sqx.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chengming
 * @since 2024/7/12
 * 线程池配置
 */
@Slf4j
@Configuration
@EnableAsync(proxyTargetClass = true)
public class ThreadPoolConfig {
    //配置队列大小
    private static final int queueCapacity = 100;
    //配置线程池中的线程的名称前缀
    private static final String namePrefix = "thread";
    //线程池维护线程所允许的空闲时间
    private static final int keepAliveSeconds = 60;

    @Bean(name = "asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        log.info("初始化异步线程池");
        //获取cpu核心数，最大线程数和核心线程数为cpu核心数的2倍
        int currSystemThreads = Runtime.getRuntime().availableProcessors() + 1;
        log.info("系统可用线程池个数:{}", currSystemThreads);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(currSystemThreads);
        //配置最大线程数
        executor.setMaxPoolSize(currSystemThreads * 2);
        //配置队列大小
        executor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);
        //线程池维护线程所允许的空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行--拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        //等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
