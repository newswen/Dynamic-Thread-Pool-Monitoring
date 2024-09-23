package com.yw.sdk.config;

import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import com.yw.sdk.domain.register.IRegister;
import com.yw.sdk.domain.register.redis.RedisRegister;
import com.yw.sdk.domain.service.DynamicThreadPoolServiceImpl;
import com.yw.sdk.domain.trigger.job.RefreshThreadPoolSettingJob;
import com.yw.sdk.domain.trigger.listener.ThreadPoolConfigAdjustListener;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
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
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
@EnableScheduling
public class DynamicThreadPoolAutoConfig {
    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private String applicationName;

    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolServiceImpl dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, RedissonClient redissonClient) {
        //获取当前启动类的服务名称
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (applicationName == null) {
            applicationName = "服务名称未配置！";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }
        //这部分的内容就是服务启动时将配置的线程池信息注册到注册中心中
        //如果是初次启动，不将配置信息注册到注册中心，由定时任务进行刷新配置进行保存
        //这里是为了防止服务重启，导致之前的线程池配置又回到初始yaml配置了
        //对线程池Map进行获取打印
        for (String threadPoolName : threadPoolExecutorMap.keySet()) {
            String redisKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolName;
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(redisKey).get();
            if (threadPoolConfigEntity != null) {
                ThreadPoolExecutor oldThreadPoolConfigEntity = threadPoolExecutorMap.get(threadPoolName);
                oldThreadPoolConfigEntity.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
                oldThreadPoolConfigEntity.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
                logger.info("动态线程池，启动初始化提示。线程池名称：{} 设置参数为：{}", threadPoolName, threadPoolConfigEntity);
            }
        }
        return new DynamicThreadPoolServiceImpl(applicationName, threadPoolExecutorMap);
    }

    @Bean("redissonClient")
    public RedissonClient redissonClient(ConfigurableApplicationContext applicationContext, DynamicThreadPoolAutoProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;
        RedissonClient redissonClient = Redisson.create(config);
        //redissonClient.isShutdown()是检测redis客户端是否关闭了
        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }


    //注入Redisson服务
    @Bean
    public IRegister redissonRegister(RedissonClient redissonClient) {
        return new RedisRegister(redissonClient);
    }

    //注入定时任务刷新配置线程池配置
    @Bean
    public RefreshThreadPoolSettingJob refreshThreadPoolSettings(DynamicThreadPoolServiceImpl dynamicThreadPoolService, IRegister redissonRegister) {
        return new RefreshThreadPoolSettingJob(dynamicThreadPoolService, redissonRegister);
    }

    //订阅者注入
    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(DynamicThreadPoolServiceImpl dynamicThreadPoolService, IRegister redissonRegister) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, redissonRegister);
    }

    //线程池sdk配置监听器
    @Bean
    public RTopic rTopic(ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener, RedissonClient redissonClient) {
        RTopic rTopic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        //给这个设置订阅者
        rTopic.addListener(ThreadPoolConfigEntity.class,threadPoolConfigAdjustListener);
        return rTopic;
    }

}
