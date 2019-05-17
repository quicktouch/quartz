package com.th.quartz.quartzdemo.c_misfire;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.util.Date;

/**
 * <p>
 * A dumb implementation of Job, for unit testing purposes.
 * </p>
 *
 * @author James House
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class StatefulDumbJob implements Job {

    // DisallowConcurrentExecution 不允许并发执行统一个job。 如果job占用时间很长,超过了调度的间隔。那么会等待执行中的任务执行完成后再execute

    // PersistJobDataAfterExecution 使注解的状态生效,引用计数生效

    /**
     * (关键字)统计job执行的次数
     */
    public static final String NUM_EXECUTIONS = "NumExecutions";

    /**
     * (关键字)用来控制执行时间
     */
    public static final String EXECUTION_DELAY = "ExecutionDelay";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Constructors.
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public StatefulDumbJob() {
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.error("---key:" + context.getJobDetail().getKey() + " 执行时间.[" + new Date() + "]");

        JobDataMap map = context.getJobDetail().getJobDataMap();

        int executeCount = 0;
        if (map.containsKey(NUM_EXECUTIONS)) {
            executeCount = map.getInt(NUM_EXECUTIONS);
        }

        executeCount++;

        map.put(NUM_EXECUTIONS, executeCount);

        long delay = 5000l;
        if (map.containsKey(EXECUTION_DELAY)) {
            delay = map.getLong(EXECUTION_DELAY);
        }

        try {
            Thread.sleep(delay);
        } catch (Exception ignore) {
        }

        log.warn("---key" + context.getJobDetail().getKey() + " 执行结束时间.[" + new Date() + "]" + " 执行结束 (" + executeCount + ").");

    }

}
