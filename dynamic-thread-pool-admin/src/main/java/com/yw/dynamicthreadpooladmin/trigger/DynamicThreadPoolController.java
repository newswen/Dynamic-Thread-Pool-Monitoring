package com.yw.dynamicthreadpooladmin.trigger;

import com.alibaba.fastjson.JSON;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.dynamicthreadpooladmin.types.RedisRegistryEnum;
import com.yw.dynamicthreadpooladmin.types.Response;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 动态线程池控制器
 *
 * @author: yuanwen
 * @since: 2024/9/22
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/dynamic/thread/pool/")
@Slf4j
public class DynamicThreadPoolController {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 查询线程池数据
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list'
     */
    @RequestMapping(value = "query_thread_pool_list", method = RequestMethod.GET)
    public Response<List<ThreadPoolConfigEntity>> queryThreadPoolList() {
        try {
            List<ThreadPoolConfigEntity> threadPoolConfigEntityList = new ArrayList<>();
            RKeys keys = redissonClient.getKeys();
            for (String key : keys.getKeysByPattern(RedisRegistryEnum.THREAD_POOL_CONFIG_LIST_KEY.getKey() + ":*")) {
                threadPoolConfigEntityList.addAll(redissonClient.<ThreadPoolConfigEntity>getList(key).readAll());
            }
            return Response.success(threadPoolConfigEntityList);
        } catch (Exception e) {
            log.info("查询线程池异常:" + e);
            return Response.error(Response.CodeEnum.ERROR);
        }
    }


    /**
     * 查询线程池配置
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_config?appName=dynamic-thread-pool-test-app&threadPoolName=threadPoolExecutor'
     */
    @RequestMapping(value = "query_thread_pool_config", method = RequestMethod.GET)
    public Response<ThreadPoolConfigEntity> queryThreadPoolConfig(@RequestParam String appName, @RequestParam String threadPoolName) {
        try {
            String key = RedisRegistryEnum.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + appName + "_" + threadPoolName;
            RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(key);
            return Response.success(bucket.get());
        } catch (Exception e) {
            log.info("查询线程池配置异常:" + e);
            return Response.error(Response.CodeEnum.ERROR);
        }
    }

    /**
     * 修改线程池配置
     * curl --request POST \
     * --url http://localhost:8089/api/v1/dynamic/thread/pool/update_thread_pool_config \
     * --header 'content-type: application/json' \
     * --data '{
     * "appName":"dynamic-thread-pool-test-app",
     * "threadPoolName": "threadPoolExecutor",
     * "corePoolSize": 1,
     * "maximumPoolSize": 10
     * }'
     */
    @RequestMapping(value = "update_thread_pool_config", method = RequestMethod.POST)
    public Response<Boolean> updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity request) {
        try {
            RTopic rTopic = redissonClient.getTopic(RedisRegistryEnum.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + request.getAppName());
            rTopic.publish(request);
            log.info("修改线程池配置成功,线程池配置：{}", JSON.toJSONString(request));
            return Response.success(true);
        } catch (Exception e) {
            log.info("修改线程池配置异常:" + e);
            return Response.error(Response.CodeEnum.ERROR);
        }
    }

}
