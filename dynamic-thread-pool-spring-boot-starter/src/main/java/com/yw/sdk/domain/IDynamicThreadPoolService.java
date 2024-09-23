package com.yw.sdk.domain;

import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * 动态线程服务接口
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
public interface IDynamicThreadPoolService {

    //获取线程池配置列表
    List<ThreadPoolConfigEntity> getThreadPoollConfigList();

    //根据应用名称获取线程池配置
    ThreadPoolConfigEntity getThreadPoolConfigByAppName(String appName);

    //更新线程池配置
    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

}
