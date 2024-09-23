package com.yw.sdk.domain.register.redis;

import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import com.yw.sdk.domain.register.IRegister;
import javafx.application.Application;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.List;

/**
 * Redis注册中心，这里注册中心可以是redis、zookeeper、mysql等，所以需实现该接口
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
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
        if(threadPoolConfigEntityList == null || threadPoolConfigEntityList.isEmpty()){
            return;
        }
        RList<ThreadPoolConfigEntity> list = redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey()+":"+threadPoolConfigEntityList.get(0).getAppName());
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
        String key = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
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
        String key = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        if (redissonClient.getBucket(key).isExists()) {
            RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(key);
            //并设置过期时间为30天
            bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
        }
    }


}
