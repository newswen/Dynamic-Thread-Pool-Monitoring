package com.yw.dynamicthreadpooltest;

import cn.hutool.core.thread.ThreadUtil;
import com.yw.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.yw.sdk.domain.model.valobj.RegistryEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.*;

@SpringBootTest
@Slf4j
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

    public static void main(String[] args) {
        try {
            log.info("开始执行");
            test();
        } catch (Exception e) {
            log.info("异常"+e);
            e.printStackTrace();
        }
    }

    public static void test() throws InterruptedException {
//        ExecutorService executor = ThreadUtil.newExecutor();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        // kc_customer
        CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
            log.info("开始更新kc_customer");
            log.info("更新kc_customer完毕");
        }, executor);

        // kc_customer_detail_log
        CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
            log.info("开始更新kc_customer_detail_log");
            log.info("更新kc_customer_detail_log完毕");
        }, executor);

        // kc_free_course_ask
        CompletableFuture<Void> task3 = CompletableFuture.runAsync(() -> {
            log.info("开始更新kc_free_course_ask");
            log.info("更新kc_free_course_ask完毕");
        }, executor);

        // kc_sms_send_records
        CompletableFuture<Void> task4 = CompletableFuture.runAsync(() -> {
            log.info("开始更新kc_sms_send_records");
            log.info("更新kc_sms_send_records完毕");
        }, executor);

        // kc_statistics_log
        CompletableFuture<Void> task5 = CompletableFuture.runAsync(() -> {
            log.info("开始更新kc_statistics_log");
            log.info("更新kc_statistics_log完毕");
        }, executor);

        CompletableFuture.allOf(task1,task2,task3,task4,task5).join();
        log.info("更新部门code完毕");
    }

    public static void test2() throws InterruptedException, ExecutionException {
        CompletionService<Boolean> completionService = ThreadUtil.newCompletionService();
        Future<Boolean> future1 = completionService.submit((() -> {

//                Thread.sleep(100000);
            int i = 1 / 0;
            log.info("更新kc_customer完毕");
        }), true);
        Future<Boolean> future2 = completionService.submit((() -> {

//                Thread.sleep(100000);
            int i = 1 / 0;
            log.info("更新kc_customer完毕");
        }), true);
        future1.get();
        future2.get();
        //收集这些线程执行结果
        completionService.take();
        log.info("结束");
    }

    public static void test3() {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // kc_customer
        CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
//            try {
//                Thread.sleep(100000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            int i = 1 / 0;
        }, executor);
        CompletableFuture.allOf(task1).join();
        log.info("更新部门code完毕");
    }

}
