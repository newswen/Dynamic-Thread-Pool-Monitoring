package com.yw.dynamicthreadpooladmin.register.impl;

import com.alibaba.fastjson.JSON;
import com.yw.dynamicthreadpooladmin.register.IRegister;
import com.yw.dynamicthreadpooladmin.types.RedisRegistryEnum;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Redis注册中心，这里注册中心可以是redis、zookeeper、mysql等，所以需实现该接口
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
@Service
@ConditionalOnProperty(value = "dynamic.thread.pool.config.redis.enable", havingValue = "true", matchIfMissing = false)
public class RedisRegister implements IRegister {

    private Logger logger = LoggerFactory.getLogger(RedisRegister.class);

    private final RedissonClient redissonClient;

    public RedisRegister(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 将线程池配置列表注册到Redis中，方便前端进行列表展示
     *
     * @param threadPoolConfigEntityList
     */
    @Override
    public void registerThreadPoolConfigList(List<ThreadPoolConfigEntity> threadPoolConfigEntityList) {
        if (threadPoolConfigEntityList == null || threadPoolConfigEntityList.isEmpty()) {
            return;
        }
        RList<ThreadPoolConfigEntity> list = redissonClient.getList(RedisRegistryEnum.THREAD_POOL_CONFIG_LIST_KEY.getKey() + ":" + threadPoolConfigEntityList.get(0).getAppName());
        //这里为了避免每一次注册都会叠加，导致重复出现，每次注册都进行清空操作
        //9-23重构为主键+:+应用服务名，每一个应用服务对其线程池配置进行单独清空
        list.clear();
        list.addAll(threadPoolConfigEntityList);
    }

    /**
     * 将单个的线程信息注册到Redis中，方便前端进行单个查看详情获取
     *
     * @param threadPoolConfigEntity
     */
    @Override
    public void registerThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String key = RedisRegistryEnum.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(key);
        //并设置过期时间为30天
        bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
    }

    /**
     * Redis的发布订阅，当操作修改线程池配置时，监听更新
     *
     * @param threadPoolConfigEntity
     */
    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String key = RedisRegistryEnum.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        if (redissonClient.getBucket(key).isExists()) {
            RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(key);
            //并设置过期时间为30天
            bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
        }
    }

    @Override
    public void initiaProjectThreadPool(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        //这部分的内容就是服务启动时将配置的线程池信息注册到注册中心中
        //如果是初次启动，不将配置信息注册到注册中心，由定时任务进行刷新配置进行保存
        //这里是为了防止服务重启，导致之前的线程池配置又回到初始yaml配置了
        //对线程池Map进行获取打印
        for (String threadPoolName : threadPoolExecutorMap.keySet()) {
            String redisKey = RedisRegistryEnum.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolName;
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(redisKey).get();
            if (threadPoolConfigEntity != null) {
                ThreadPoolExecutor oldThreadPoolConfigEntity = threadPoolExecutorMap.get(threadPoolName);
                oldThreadPoolConfigEntity.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
                oldThreadPoolConfigEntity.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
                logger.info("动态线程池，启动初始化提示。线程池名称：{} 设置参数为：{}", threadPoolName, threadPoolConfigEntity);
            }
        }
    }

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        try {
            List<ThreadPoolConfigEntity> threadPoolConfigEntityList = new ArrayList<>();
            RKeys keys = redissonClient.getKeys();
            for (String key : keys.getKeysByPattern(RedisRegistryEnum.THREAD_POOL_CONFIG_LIST_KEY.getKey() + ":*")) {
                threadPoolConfigEntityList.addAll(redissonClient.<ThreadPoolConfigEntity>getList(key).readAll());
            }
            return threadPoolConfigEntityList;
        } catch (Exception e) {
            logger.info("查询Redis中线程池异常:" + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfig(String appName, String threadPoolName) {
        try {
            String key = RedisRegistryEnum.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + appName + "_" + threadPoolName;
            RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(key);
            return bucket.get();
        } catch (Exception e) {
            logger.info("查询Redis中线程池配置异常:" + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean updateThreadPoolConfigByAdmin(ThreadPoolConfigEntity request) {
        try {
            RTopic rTopic = redissonClient.getTopic(RedisRegistryEnum.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + request.getAppName());
            rTopic.publish(request);
            logger.info("修改线程池配置成功,线程池配置：{}", JSON.toJSONString(request));
            return true;
        } catch (Exception e) {
            logger.info("修改Redis中线程池配置异常:" + e);
            throw new RuntimeException(e);
        }
    }
}
