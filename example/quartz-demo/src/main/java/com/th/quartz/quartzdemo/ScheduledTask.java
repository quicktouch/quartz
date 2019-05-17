package com.th.quartz.quartzdemo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduledTask {
    @Scheduled(fixedRate = 5000)
    //表示每隔5000ms，Spring scheduling会调用一次该方法，不论该方法的执行时间是多少
    public void reportCurrentTime() throws InterruptedException {
        System.out.println(new Date());
    }

    @Scheduled(fixedDelay = 5000)
    //表示当方法执行完毕5000ms后，Spring scheduling会再次调用该方法
    public void reportCurrentTimeAfterSleep() throws InterruptedException {
        System.out.println(new Date());
    }

    @Scheduled(cron = "0 0 1 * * *")
    //提供了一种通用的定时任务表达式，这里表示每隔5秒执行一次，更加详细的信息可以参考cron表达式。
    public void reportCurrentTimeCron() throws InterruptedException {
        System.out.println(new Date());
    }
}
