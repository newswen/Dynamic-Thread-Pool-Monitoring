package com.yw.sdk.domain.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.yw.sdk.config.DynamicThreadPoolAutoConfig;
import com.yw.sdk.domain.IDynamicThreadPoolService;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.register.IRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * 线程池配置更新监听器-Redisson的发布订阅
 *
 * @author: yuanwen
 * @since: 2024/9/19
 */
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegister redissonRegister;

    public ThreadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegister redissonRegister) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.redissonRegister = redissonRegister;
    }

    //这里会监听到对应队列的消息，在前端进行修改线程池配置时
    //触发接口，向Redisson中publis消息，这里进行接收
    //和
    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
        logger.info("监听到线程池配置更新消息：{}", JSON.toJSONString(threadPoolConfigEntity));
        //1.更新传入的线程池配置
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        //2.导入本sdk的服务对应的线程池列表进行更新
        List<ThreadPoolConfigEntity> threadPoolConfigEntityList = dynamicThreadPoolService.getThreadPoollConfigList();
        redissonRegister.registerThreadPoolConfigList(threadPoolConfigEntityList);
        //3.然后更新导入本sdk的服务对应的线程池单个配置进行更新
        //注意这里如果传入了未知的线程池，也会加入到Redis配置中，后续需要进行判断处理

        //9-22新加判断是否存在于Redis中
        redissonRegister.updateThreadPoolConfig(threadPoolConfigEntity);
        logger.info("更新线程池配置成功,订阅结束！");
    }
}
