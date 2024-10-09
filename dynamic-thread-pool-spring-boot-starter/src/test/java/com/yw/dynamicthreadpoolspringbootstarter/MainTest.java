package com.yw.dynamicthreadpoolspringbootstarter;

import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(
                3, // 核心线程数为3
                10, // 最大线程数为10
                6L, TimeUnit.MILLISECONDS, // 空闲线程存活时间0毫秒
                new LinkedBlockingQueue<>(10)); // 阻塞队列大小为10
        StopWatch watch = new StopWatch("MainTest"); // 创建一个计时器
        watch.start(); // 开始计时

        CountDownLatch countDownLatch = new CountDownLatch(5); // 创建一个倒计时器，初始值为5

        for (int i = 0; i < 5; i++) { // 循环提交5个任务到线程池
            final int finalI = i;
            executorService.submit(() -> {
                try {
                    System.out.println("当前线程" + Thread.currentThread().getName() + ",--【任务" + finalI + "】开始执行---");
                    List<String> arrayList = getDataFromDB(); // 模拟从数据库查询数据并对其进行处理
                    CountDownLatch countDownLatchSub = new CountDownLatch(arrayList.size());
                    for (String str : arrayList) {
                        CompletableFuture<String> future = handleEvent(str, finalI, executorService,countDownLatchSub);
                    }
                    countDownLatchSub.await();
                    System.out.println("当前线程" + Thread.currentThread().getName() + ",--【任务" + finalI + "】执行完成---");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown(); // 完成一个任务，倒计时减一
                }
            });
        }

        countDownLatch.await(); // 主线程等待所有子线程执行完毕
        watch.stop(); // 结束计时
        System.out.println(watch.prettyPrint()); // 输出计时结果
    }

    public static CompletableFuture<String> handleEvent(String str, Integer finalI, ExecutorService executorService,CountDownLatch countDownLatchSub) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("当前线程" + Thread.currentThread().getName() + "[任务" + finalI + "]开始处理数据-" + str);
                TimeUnit.SECONDS.sleep(1); // 模拟耗时操作，休眠1秒
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                countDownLatchSub.countDown();
            }
            return "Success";
        }, executorService);
    }

    private static List<String> getDataFromDB() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("1");
        arrayList.add("2");
        return arrayList;
    }
}