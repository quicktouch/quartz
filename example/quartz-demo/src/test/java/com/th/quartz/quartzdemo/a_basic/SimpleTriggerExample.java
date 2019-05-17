package com.th.quartz.quartzdemo.a_basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.text.ParseException;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class SimpleTriggerExample {


    public static void main(String[] args) throws SchedulerException, ParseException {

        new SimpleTriggerExample().simpleTrigger();

        //new SimpleTriggerExample().cronTrigger();
    }

    @Test
    public void simpleTrigger() throws SchedulerException {

        //实现job接口,可以使java类变为可调度的任务
        //创建job实例
        JobDetailImpl jobDetail = new JobDetailImpl();
        //name和group可以确定唯一一个job
        jobDetail.setName("myJob");
        jobDetail.setGroup("myJobGroup");
        jobDetail.setJobClass(MyJob.class);


        //创建simplerTrigger对象
        SimpleTriggerImpl trigger = new SimpleTriggerImpl();
        //name和group可以确定唯一一个trigger
        trigger.setName("myTrigger");
        trigger.setGroup("myTriggerGroup");
        //设置时间触发规则,下一分钟执行
        //下一分钟执行
        Date runTime = evenMinuteDate(new Date());
        trigger.setStartTime(runTime);
        trigger.setRepeatCount(10);
        trigger.setRepeatInterval(1000 * 3);

        //创建scheduler对象
        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();

        //SchedulerFactory总注册jobDetail和trigger
        scheduler.scheduleJob(jobDetail, trigger);

        //启动调度任务
        scheduler.start();

        try {
            // wait 90 seconds to show job
            Thread.sleep(90L * 1000L);
            // executing...
        } catch (Exception e) {
            //
        }
        // shut down the scheduler
        log.info("------- Shutting Down ---------------------");
        scheduler.shutdown(true);
        log.info("------- Shutdown Complete -----------------");
    }

    @Test
    public void cronTrigger() throws ParseException, SchedulerException {

        //基于cron表达式
        //可以设置比较复杂的时间间隔的情况
        //支持日历相关的重复时间间隔,例如每个月1号执行一次

        //Cron表达式的基本语法
        //由6-7个空格分割的时间字段组成
        //对特殊字符的大小写不敏感

        //使用建造者模式简化
        JobDetail jobDetail = JobBuilder.newJob(MyJob.class).withIdentity("myjob", "myjobGrounp").build();

        CronTriggerImpl trigger = new CronTriggerImpl();
        trigger.setName("myCronTrigger");
        trigger.setGroup("myCronTriggerGroup");
        //时间触发规则 2019年5月15号 14点18分 执行 每隔3秒执行一次
        trigger.setCronExpression("0/3 48 14 16 5 ? 2019");

        //CronTrigger trigger2 = newTrigger()
        //        .withIdentity("myCronTrigger", "myCronTriggerGroup")
         //       .withSchedule(cronSchedule("0/3 48 14 16 5 ? 2019"))
         //       .build();

        //创建scheduler对象
        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();

        //SchedulerFactory总注册jobDetail和trigger
        scheduler.scheduleJob(jobDetail, trigger);

        //启动调度任务
        scheduler.start();
    }

}
