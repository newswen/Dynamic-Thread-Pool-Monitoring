package com.yw.dynamicthreadpooladmin.trigger;

import com.alibaba.nacos.api.exception.NacosException;
import com.yw.dynamicthreadpooladmin.register.IRegister;
import com.yw.dynamicthreadpooladmin.types.Response;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private IRegister register;

    /**
     * 查询线程池数据
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list'
     */
    @RequestMapping(value = "query_thread_pool_list", method = RequestMethod.GET)
    public Response<List<ThreadPoolConfigEntity>> queryThreadPoolList() throws NacosException {
        try {
            List<ThreadPoolConfigEntity> threadPoolConfigEntityList = register.queryThreadPoolList();
            return Response.success(threadPoolConfigEntityList);
        } catch (Exception e) {
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
            ThreadPoolConfigEntity threadPoolConfigEntity = register.queryThreadPoolConfig(appName, threadPoolName);
            return Response.success(threadPoolConfigEntity);
        } catch (Exception e) {
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
            return Response.success(register.updateThreadPoolConfigByAdmin(request));
        } catch (Exception e) {
            return Response.error(Response.CodeEnum.ERROR);
        }
    }
}
