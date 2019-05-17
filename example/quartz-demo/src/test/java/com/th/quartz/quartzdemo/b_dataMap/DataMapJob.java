package com.th.quartz.quartzdemo.b_dataMap;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.util.Date;

@Slf4j
public class DataMapJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        log.info("jobkey:{} param:{}", jobKey, jobDataMap.getString("name"));
    }
}
