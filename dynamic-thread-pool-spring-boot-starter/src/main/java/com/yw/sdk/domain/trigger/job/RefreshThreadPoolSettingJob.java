package com.yw.sdk.domain.trigger.job;

import com.alibaba.fastjson.JSON;
import com.yw.sdk.config.DynamicThreadPoolAutoConfig;
import com.yw.sdk.domain.IDynamicThreadPoolService;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.register.IRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;


import java.util.List;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
public class RefreshThreadPoolSettingJob {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegister register;

    public RefreshThreadPoolSettingJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegister register) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.register = register;
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void refreshThreadPoolSettings() {
        List<ThreadPoolConfigEntity> threadPoolConfigEntityList = dynamicThreadPoolService.getThreadPoollConfigList();
        register.registerThreadPoolConfigList(threadPoolConfigEntityList);
        logger.info("动态线程池，上报线程池信息：{}", JSON.toJSONString(threadPoolConfigEntityList));
        threadPoolConfigEntityList.forEach(threadPoolConfigEntity -> {
            register.registerThreadPoolConfig(threadPoolConfigEntity);
            logger.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
        });
    }
}
