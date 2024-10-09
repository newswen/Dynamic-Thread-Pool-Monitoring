package com.yw.sdk.domain.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.listener.impl.PropertiesListener;
import com.yw.sdk.config.DynamicThreadPoolAutoConfig;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/27
 */
public class NacosConfigListener implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private final ConfigService configService;

    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    private final String applicationName;

    public NacosConfigListener(ConfigService configService, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, String applicationName) {
        this.configService = configService;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
        this.applicationName = applicationName;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!threadPoolExecutorMap.isEmpty() && configService != null) {
            threadPoolExecutorMap.forEach((threadPoolName, threadPoolExecutor) -> {
                try {
                    String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "-" + applicationName + "-" + threadPoolName + ".yml";
                    configService.addListener(dataId, "DEFAULT_GROUP", new PropertiesListener() {
                        @Override
                        public void innerReceive(Properties properties) {
                            logger.info("动态线程池{}，监听配置文件变化:{}",threadPoolName,JSON.toJSONString(properties));
                            threadPoolExecutor.setCorePoolSize(Integer.parseInt(properties.getProperty("corePoolSize")));
                            threadPoolExecutor.setMaximumPoolSize(Integer.parseInt(properties.getProperty("maximumPoolSize")));
                            logger.info("动态线程池{}，监听配置修改线程池配置信息成功,修改后配置为：{}" ,threadPoolName, JSON.toJSONString(threadPoolExecutor));
                        }
                    });
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
