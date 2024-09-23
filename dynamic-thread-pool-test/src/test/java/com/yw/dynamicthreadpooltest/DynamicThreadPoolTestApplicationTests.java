package com.yw.dynamicthreadpooltest;

import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import org.junit.jupiter.api.Test;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class DynamicThreadPoolTestApplicationTests {

    @Resource
    private RedissonClient redissonClient;


    @Test
    void contextLoads() {

        RTopic rTopic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + "dynamic-thread-pool-test");
        ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity("test", "threadPollExecutorOne");
        threadPoolConfigEntity.setActiveCount(1);
        threadPoolConfigEntity.setCorePoolSize(1);
        threadPoolConfigEntity.setMaximumPoolSize(1);
        rTopic.publish(threadPoolConfigEntity);

    }

}
