package com.yw.sdk.config;


import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import com.yw.sdk.domain.register.IRegister;
import com.yw.sdk.domain.register.redis.NacosRegister;
import com.yw.sdk.domain.register.redis.RedisRegister;
import com.yw.sdk.domain.service.DynamicThreadPoolServiceImpl;
import com.yw.sdk.domain.trigger.job.RefreshThreadPoolSettingJob;
import com.yw.sdk.domain.trigger.listener.NacosConfigListener;
import com.yw.sdk.domain.trigger.listener.ThreadPoolConfigAdjustListener;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
@Configuration
@EnableScheduling
public class DynamicThreadPoolAutoConfig {
    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private String applicationName;

    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolServiceImpl dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, IRegister register) throws NacosException {
        //获取当前启动类的服务名称
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (applicationName == null) {
            applicationName = "服务名称未配置！";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }
        System.out.println("当前应用名称：" + applicationName);
        register.initiaProjectThreadPool(applicationName, threadPoolExecutorMap);
        return new DynamicThreadPoolServiceImpl(applicationName, threadPoolExecutorMap);
    }

    //注入Redisson服务
    @Bean("register")
    @ConditionalOnProperty(value = "dynamic.thread.pool.config.redis.enable", havingValue = "true", matchIfMissing = false)
    @ConditionalOnMissingBean
    public IRegister redissonRegister(RedissonClient redissonClient) {
        logger.info("使用Redisson作为注册中心");
        return new RedisRegister(redissonClient);
    }

    //注入Nacos服务
    @Bean("register")
    @ConditionalOnProperty(value = "dynamic.thread.pool.config.nacos.enable", havingValue = "true", matchIfMissing = false)
    @ConditionalOnMissingBean
    public IRegister naCosRegister(ConfigService configService) {
        logger.info("使用Nacos作为注册中心");
        return new NacosRegister(configService);
    }

    //注入定时任务刷新配置线程池配置
    @Bean
    public RefreshThreadPoolSettingJob refreshThreadPoolSettings(DynamicThreadPoolServiceImpl dynamicThreadPoolService, IRegister register) {
        return new RefreshThreadPoolSettingJob(dynamicThreadPoolService, register);
    }

    //订阅者注入
    @Bean
    @ConditionalOnProperty(value = "dynamic.thread.pool.config.redis.enable", havingValue = "true", matchIfMissing = false)
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(DynamicThreadPoolServiceImpl dynamicThreadPoolService, IRegister register) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, register);
    }

    //线程池sdk配置监听器
    @Bean
    @ConditionalOnProperty(value = "dynamic.thread.pool.config.redis.enable", havingValue = "true", matchIfMissing = false)
    public RTopic rTopic(ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener, RedissonClient redissonClient) {
        RTopic rTopic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        //给这个设置订阅者
        rTopic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);
        return rTopic;
    }

    @Bean
    @ConditionalOnProperty(value = "dynamic.thread.pool.config.nacos.enable", havingValue = "true", matchIfMissing = false)
    public NacosConfigListener nacosConfigListener(Map<String, ThreadPoolExecutor> threadPoolExecutorMap, ConfigService configService) {
        return new NacosConfigListener(configService, threadPoolExecutorMap, applicationName);
    }

}
