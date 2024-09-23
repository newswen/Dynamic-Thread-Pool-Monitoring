package com.yw.dynamicthreadpooltest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/23
 */
@RestController
@RequestMapping("/test")
public class ThreadPoolTestController {

    @Resource
    private ThreadPoolExecutor threadPollExecutorOne;

    @RequestMapping("/up")
    public String up(){
        threadPollExecutorOne.execute(() -> {
            System.out.println("线程池消耗1");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return "up";
    }

}
