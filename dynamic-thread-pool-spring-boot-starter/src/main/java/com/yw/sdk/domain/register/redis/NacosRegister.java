package com.yw.sdk.domain.register.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import com.yw.sdk.domain.register.IRegister;
import org.redisson.api.RKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Nacos注册中心实现
 *
 * @author: yuanwen
 * @since: 2024/9/25
 */
public class NacosRegister implements IRegister {

    private static final Logger logger = LoggerFactory.getLogger(NacosRegister.class);

    private final ConfigService configService;

    public NacosRegister(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 将线程池配置列表注册到Nacos中，方便前端进行列表展示
     *
     * @param threadPoolConfigEntityList
     */
    @Override
    public void registerThreadPoolConfigList(List<ThreadPoolConfigEntity> threadPoolConfigEntityList) {
        if (threadPoolConfigEntityList == null || threadPoolConfigEntityList.isEmpty()) {
            return;
        }
        StringBuilder configContent = new StringBuilder();
        for (ThreadPoolConfigEntity entity : threadPoolConfigEntityList) {
            Yaml yaml = new Yaml();
            // 创建一个 Map 来保存整个配置文档
            Map<String, ThreadPoolConfigEntity> configMap = new HashMap<>();
            configMap.put(entity.getThreadPoolName(), entity);
            String property = yaml.dumpAsMap(configMap);
            configContent.append(property);
        }
        try {
            String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey() + ".yml";
            configService.publishConfig(dataId, "DEFAULT_GROUP", configContent.toString(), "yaml");
        } catch (NacosException e) {
            logger.error("线程池配置列表注册到Nacos中-失败", e);
        }
    }

    /**
     * 将单个的线程信息注册到Nacos中，方便前端进行单个查看详情获取
     *
     * @param threadPoolConfigEntity
     */
    @Override
    public void registerThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "-" + threadPoolConfigEntity.getAppName() + "-" + threadPoolConfigEntity.getThreadPoolName() + ".yml";
        StringBuilder configContent = new StringBuilder();
        Yaml yaml = new Yaml();
        // 创建一个 Map 来保存整个配置文档
        Map<String, ThreadPoolConfigEntity> configMap = new HashMap<>();
        configMap.put(threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity);
        String property = yaml.dumpAsMap(configMap);
        configContent.append(property);
        try {
            configService.publishConfig(dataId, "DEFAULT_GROUP", configContent.toString(), "yaml");
        } catch (NacosException e) {
            logger.error("线程池配置单个注册到Nacos中-失败", e);
        }
    }

    /**
     * Nacos的发布订阅，当操作修改线程池配置时，监听更新
     *
     * @param threadPoolConfigEntity
     */
    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        try {
            configService.publishConfig(dataId, "DEFAULT_GROUP", threadPoolConfigEntity.toString());
        } catch (NacosException e) {
            logger.error("Failed to update thread pool config in Nacos", e);
        }
    }

    @Override
    public void initiaProjectThreadPool(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) throws NacosException {
        // 启动的时候读取配置中心的配置
        String content = configService.getConfig("redis.yml", "DEFAULT_GROUP", 5000);
        System.out.println("配置内容：");
        System.out.println(content);

        // 监听配置中心数据的变化
        configService.addListener("pool.yaml", "DEFAULT_GROUP", new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                System.out.println("recieve:" + configInfo);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
    }

    /**
     * 查询线程池数据
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list'
     */
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        try {
            String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey() + ".yml";
            // 启动的时候读取配置中心的配置
            String content = configService.getConfig(dataId, "DEFAULT_GROUP", 5000);
            Yaml yaml = new Yaml();
            Map<String, ThreadPoolConfigEntity> map = yaml.load(content);
            System.out.println(JSON.toJSONString(map));
            return new ArrayList<>(map.values());
        } catch (Exception e) {
            logger.info("查询查询nacos中线程池异常:" + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfig(String appName, String threadPoolName) {
        try {
            String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "-" + appName + "-" + threadPoolName + ".yml";
            // 启动的时候读取配置中心的配置
            String content = configService.getConfig(dataId, "DEFAULT_GROUP", 5000);
            Yaml yaml = new Yaml();
            Map<String, ThreadPoolConfigEntity> map = yaml.load(content);
            return map.get(threadPoolName);
        } catch (Exception e) {
            logger.info("查询nacos中线程池配置异常:" + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean updateThreadPoolConfigByAdmin(ThreadPoolConfigEntity request) {
        try {
            String appName = request.getAppName();
            String threadPoolName = request.getThreadPoolName();
            String dataId = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "-" + appName + "-" + threadPoolName + ".yml";
            // 启动的时候读取配置中心的配置
            String content = configService.getConfig(dataId, "DEFAULT_GROUP", 5000);
            Yaml yaml = new Yaml();
            Map<String, ThreadPoolConfigEntity> configMap = yaml.load(content);
            ThreadPoolConfigEntity threadPoolConfigEntity = configMap.get(threadPoolName);
            threadPoolConfigEntity.setCorePoolSize(request.getCorePoolSize());
            threadPoolConfigEntity.setMaximumPoolSize(request.getMaximumPoolSize());

            String property = yaml.dumpAsMap(configMap);

            boolean result = configService.publishConfig(dataId, "DEFAULT_GROUP", property, "yaml");
            if (result) {
                logger.info("修改线程池配置成功,线程池配置：{}", JSON.toJSONString(request));
                return true;
            }
            logger.info("修改线程池配置失败");
            throw new RuntimeException("修改线程池配置失败");
        } catch (Exception e) {
            logger.info("修改线程池配置异常:" + e);
            throw new RuntimeException(e);
        }
    }
}