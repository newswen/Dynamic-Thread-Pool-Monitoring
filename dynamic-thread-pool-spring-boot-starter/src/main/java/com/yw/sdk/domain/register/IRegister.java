package com.yw.sdk.domain.register;

import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * 动态线程池Redis注册中心配置
 *
 * @author: yuanwen
 * @since: 2024/9/18
 */
public interface IRegister {

    //将全部的线程池配置信息列表注册到redis中
    void registerThreadPoolConfigList(List<ThreadPoolConfigEntity> threadPoolConfigEntityList);

    //对全部的线程池配置信息挨个注册到redis中，通过服务名-线程池名称作为key
    void registerThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

}
