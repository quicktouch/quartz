package com.th.quartz.quartzdemo.b_dataMap;

import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.text.ParseException;
import java.util.Date;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class DataMapExample {

    @Test
    public void start() throws SchedulerException {

        //将参数添加到jobDetail中
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("name", "Xiaoming");

        JobDetail jobDetail = JobBuilder.newJob(DataMapJob.class)
                .withDescription("传递参数实例 JobDetail")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();

        //也可以这样
        //jobDetail.getJobDataMap().put("key","value");

        SimpleTrigger trigger = newTrigger()
                .withIdentity("trigger")
                .startAt(evenMinuteDate(new Date()))
                .withSchedule(simpleSchedule().withIntervalInSeconds(3).withRepeatCount(10))
                .build();

        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();

        try {
            // wait 90 seconds to show job
            Thread.sleep(90L * 1000L);
            // executing...
        } catch (Exception e) {
            //
        }
    }

}
