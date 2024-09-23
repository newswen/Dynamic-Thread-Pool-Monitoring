package com.yw.dynamicthreadpooltest.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {


    @Bean("threadPollExecutorOne")
    public ThreadPoolExecutor dynamicThreadPoolOne(ThreadPoolConfigProperties threadPoolConfigProperties) {
        //根据线程池配置参数，设置拒绝策略
        RejectedExecutionHandler handler;
        switch (threadPoolConfigProperties.getRejectPolicy()) {
            //丢弃任务不抛出异常
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            //丢弃最老的任务，执行新任务
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            //多余的任务交给主线程执行
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            //丢弃任务抛出异常
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
        }


        return new ThreadPoolExecutor(
                threadPoolConfigProperties.getCorePoolSize(),
                threadPoolConfigProperties.getMaxPoolSize(),
                threadPoolConfigProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(threadPoolConfigProperties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);
    }

    @Bean("threadPollExecutorTwo")
    public ThreadPoolExecutor dynamicThreadPoolTwo(ThreadPoolConfigProperties threadPoolConfigProperties) {
        //根据线程池配置参数，设置拒绝策略
        RejectedExecutionHandler handler;
        switch (threadPoolConfigProperties.getRejectPolicy()) {
            //丢弃任务不抛出异常
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            //丢弃最老的任务，执行新任务
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            //多余的任务交给主线程执行
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            //丢弃任务抛出异常
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
        }

        return new ThreadPoolExecutor(
                threadPoolConfigProperties.getCorePoolSize(),
                threadPoolConfigProperties.getMaxPoolSize(),
                threadPoolConfigProperties.getKeepAliveTime(),
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(threadPoolConfigProperties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);

    }
}
