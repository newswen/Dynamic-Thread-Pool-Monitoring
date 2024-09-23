package com.yw.dynamicthreadpooltest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池参数字段读取配置
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
@Data
@ConfigurationProperties(prefix = "thread.pool.executor",ignoreInvalidFields = true)
public class ThreadPoolConfigProperties {

        /**
         * 线程池核心线程数
         */
        private Integer corePoolSize = 20;

        /**
         * 线程池最大线程数
         */
        private Integer maxPoolSize = 20;

        /**
         * 线程池线程空闲时间
         */
        private Integer keepAliveTime = 60;

        /**
         * 线程池任务等待队列大小
         */
        private Integer blockQueueSize = 200;

        /**
         * 线程池拒绝策略(默认为把任务拒绝并抛出异常）
         */
        private String rejectPolicy = "AbortPolicy";



}
