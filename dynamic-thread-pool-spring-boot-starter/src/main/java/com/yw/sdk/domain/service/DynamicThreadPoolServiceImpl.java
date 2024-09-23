package com.yw.sdk.domain.service;

import com.alibaba.fastjson.JSON;
import com.yw.sdk.domain.IDynamicThreadPoolService;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
public class DynamicThreadPoolServiceImpl implements IDynamicThreadPoolService {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolServiceImpl.class);


    private final String applicationName;

    //服务启动聚合线程池Map
    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolServiceImpl(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> getThreadPoollConfigList() {
        List<ThreadPoolConfigEntity> threadPoolConfigEntityList = new ArrayList<>();
        threadPoolExecutorMap.forEach((k, v) -> {
            //应用、线程名
            ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity(applicationName, k);
            //核心线程数
            threadPoolConfigEntity.setCorePoolSize(v.getCorePoolSize());
            //最大线程数
            threadPoolConfigEntity.setMaximumPoolSize(v.getMaximumPoolSize());
            //当前活跃线程数
            threadPoolConfigEntity.setActiveCount(v.getActiveCount());
            //返回线程池中当前活动的线程数量
            threadPoolConfigEntity.setPoolSize(v.getPoolSize());
            //当前线程阻塞队列大小
            threadPoolConfigEntity.setQueueSize(v.getQueue().size());
            //当前线程阻塞队列类型
            threadPoolConfigEntity.setQueueType(v.getQueue().getClass().getSimpleName());
            //当前线程阻塞队列剩余容量
            threadPoolConfigEntity.setRemainingCapacity(v.getQueue().remainingCapacity());
            threadPoolConfigEntityList.add(threadPoolConfigEntity);
        });
        return threadPoolConfigEntityList;
    }

    @Override
    public ThreadPoolConfigEntity getThreadPoolConfigByAppName(String threadPoolName) {
        if (threadPoolExecutorMap.containsKey(threadPoolName)) {
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolName);
            ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity(applicationName, threadPoolName);
            threadPoolConfigEntity.setActiveCount(threadPoolExecutor.getActiveCount());
            threadPoolConfigEntity.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
            threadPoolConfigEntity.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
            threadPoolConfigEntity.setPoolSize(threadPoolExecutor.getPoolSize());
            threadPoolConfigEntity.setQueueSize(threadPoolExecutor.getQueue().size());
            threadPoolConfigEntity.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
            threadPoolConfigEntity.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());
            logger.info("动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, threadPoolName, JSON.toJSONString(threadPoolConfigEntity));
            return threadPoolConfigEntity;
        }
        return new ThreadPoolConfigEntity(applicationName, threadPoolName);
    }

    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if (threadPoolConfigEntity == null || !applicationName.equals(threadPoolConfigEntity.getAppName())) {
            return;
        }
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if (threadPoolExecutor != null) {
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        }
    }
}
