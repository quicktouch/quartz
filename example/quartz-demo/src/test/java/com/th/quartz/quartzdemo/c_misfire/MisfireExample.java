package com.th.quartz.quartzdemo.c_misfire;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class MisfireExample {

    @Test
    public void start() throws SchedulerException, InterruptedException {


        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        //**************【第一个job,每隔3秒调用一次,但是job每次工作10秒】********************************************
        //由于限制了并发,因此可以看到每次执行相同job的时间间隔为10秒

        JobDataMap dataMap = new JobDataMap();
        //存入让job运行的时间
        dataMap.put(StatefulDumbJob.EXECUTION_DELAY, 10000L);

        JobDetail job = newJob(StatefulDumbJob.class).withIdentity("statefulJob1", "group1")
                .usingJobData(dataMap).build();

        //代表时间,整15秒,例如每分钟的15 30 45 60
        Date startTime = DateBuilder.nextGivenSecondDate(null, 15);
        SimpleTrigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(startTime)
                .withSchedule(
                        simpleSchedule().withIntervalInSeconds(3).repeatForever() // 没有指定misfire策略: 进行后一个任务
                ).build();

        Date ft = sched.scheduleJob(job, trigger);
        log.info(job.getKey() + " 将运行于: " + ft + "重复次数: " + trigger.getRepeatCount() + " 时间间隔:"
                + trigger.getRepeatInterval() / 1000 + "秒");

        sched.start();

        //***************【第二个job,】*********************************************
        job = newJob(StatefulDumbJob.class).withIdentity("statefulJob2", "group1")
                .usingJobData(StatefulDumbJob.EXECUTION_DELAY, 10000L).build();
        trigger = newTrigger()
                .withIdentity("trigger2", "group1")
                .startAt(startTime)
                .withSchedule(simpleSchedule().withIntervalInSeconds(3).repeatForever()
                        .withMisfireHandlingInstructionNowWithExistingCount()) // misfire策略:尽快重复触发
                .build();
        ft = sched.scheduleJob(job, trigger);
        log.info(job.getKey() + " 将运行于: " + ft + "重复次数: " + trigger.getRepeatCount() + " 时间间隔:"
                + trigger.getRepeatInterval() / 1000 + "秒");


        Thread.sleep(600L * 1000L);
        sched.shutdown(true);
    }
}
