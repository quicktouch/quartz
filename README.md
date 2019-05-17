# quartz

Quartz是一个功能丰富的开源作业调度库，几乎可以集成在任何Java应用程序中 - 从最小的独立应用程序到最大的电子商务系统。

## 基本概念

- 开发会遇到***任务调度***问题。 例如，按月生成统计报表
- 实现***任务调度***需要关心线程、以及资源分配问题
- 重复造轮子没必要,这里引出了Quartz

`Quartz`就是用来执行定时任务的的框架。特定时间去干某些事,例如:

> 每天5点半收拾收拾下班

Quartz中有几个特别重要的概念:`scheduler`调度器、`trigger`触发器、`job`任务,可以类比为:

名称 | 作用 | 类比
---| --- | ---
trigger| 定义调度时间 | 每天五点半
job | 被调度的任务 | 收拾东西下班
Scheduler | 执行调度的控制器 |  大脑进行协调指挥、关联前两者


## 示例(springboot) 

首先添加maven依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 简单示例

1). 实现job类,主要描述具体干了什么

```java 
public class MyJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("start job at:" + new Date());
    }
}
```

2). 使用`SimpleTriggerImpl`的示例

```java
public class QuartzSimpleTriggerTest {

    public static void main(String[] args) throws SchedulerException {

        //实现job接口,可以使java类变为可调度的任务
        //创建描述job的jobDetail对象
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setName("myjob");
        jobDetail.setGroup("myjobGrounp");
        jobDetail.setJobClass(MyJob.class);

        //创建simplerTrigger对象
        SimpleTriggerImpl trigger = new SimpleTriggerImpl();
        trigger.setName("myTrigger");
        trigger.setGroup("myTriggerGroup");

        //触发的时间规则
        trigger.setStartTime(new Date());
        trigger.setRepeatCount(10);
        trigger.setRepeatInterval(1000 * 3);

        //创建scheduler对象
        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();

        //SchedulerFactory总注册jobDetail和trigger
        scheduler.scheduleJob(jobDetail, trigger);

        //启动调度任务
        scheduler.start();
    }
}
```

3). 使用`CronTriggerImpl`的示例

```java
public static void main(String[] args) throws SchedulerException, ParseException {

    //实现job接口,可以使java类变为可调度的任务
    //创建描述job的jobDetail对象
    JobDetailImpl jobDetail = new JobDetailImpl();
    jobDetail.setName("myjob");
    jobDetail.setGroup("myjobGrounp");
    jobDetail.setJobClass(MyJob.class);

    CronTriggerImpl trigger = new CronTriggerImpl();
    trigger.setName("myTrigger");
    trigger.setGroup("myTriggerGroup");
    //时间触发规则 2019年5月15号 14点18分 执行 每隔3秒执行一次
    trigger.setCronExpression("0/3 18 14 15 5 ? 2019");

    //创建scheduler对象
    StdSchedulerFactory factory = new StdSchedulerFactory();
    Scheduler scheduler = factory.getScheduler();

    //SchedulerFactory总注册jobDetail和trigger
    scheduler.scheduleJob(jobDetail, trigger);

    //启动调度任务
    scheduler.start();
}
```

### spring boot

配置类编写,项目启动时执行定时任务。

```java
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail testQuartz1() {
        return JobBuilder.newJob(TestTask1.class).withIdentity("testTask1").storeDurably().build();
    }

    @Bean
    public Trigger testQuartzTrigger1() {
        //5秒执行一次
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(5)
                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(testQuartz1())
                .withIdentity("testTask1")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail testQuartz2() {
        return JobBuilder.newJob(TestTask2.class).withIdentity("testTask2").storeDurably().build();
    }

    @Bean
    public Trigger testQuartzTrigger2() {
        //cron方式，每隔5秒执行一次
        return TriggerBuilder.newTrigger().forJob(testQuartz2())
                .withIdentity("testTask2")
                .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?"))
                .build();
    }
}
```

job的实现类:

```java
public class TestTask1 extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("TestQuartz01----" + sdf.format(new Date()));
    }
}

public class TestTask2 extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("TestQuartz02----" + sdf.format(new Date()));
    }
}
```

## 详解

### 相关概念脑图

![20190517155805910673112.png](http://image.tyrad.cc/20190517155805910673112.png)

![20190517155806280034131.png](http://image.tyrad.cc/20190517155806280034131.png)

### 核心元素

***trigger***

trigger 是用于定义调度时间的元素，即按照什么时间规则去执行任务。Quartz 中主要提供了:

- `SimpleTrigger` 常用来处理按时间间隔执行的任务
- `CronTirgger` 使用cron表达式触发定时任务,如每周一执行
- `CalendarIntervalTrigger` 根据一个给定的日历时间进行重复，可以设置启动时间。它可以完成 SimpleTrigger（比如每个月，因为月不是一个确定的秒数）和CronTrigger（比如5个月，因为5个月并不是12个月的公约数）不能完成的一些任务。注意，使用month作为周期单位时，如果起始日期是在某月的最后一天，比如1月31日，那么下一个激活日在2月28日，以后所有的激活日都在当月的28日。如果你要严格限制在每月的最后一天激活，那你需要使用cronTrigger。不受夏令时引起的时钟偏移影响
- `DailyTimeIntervalTrigger` 在某个时间段以某个频率(如每隔一分钟)触发(重复周期必须在1天以内，不能为星期，月 ?)

***job***

job 用于表示被调度的任务。主要有两种类型的 job：无状态的（stateless）和有状态的（stateful）。对于同一个 trigger 来说，有状态的 job 不能被并行执行，只有上一次触发的任务被执行完之后，才能触发下一次执行。Job 主要有两种属性：volatility 和 durability，其中 volatility 表示任务是否被持久化到数据库存储，而 durability 表示在没有 trigger 关联的时候任务是否被保留。两者都是在值为 true 的时候任务被持久化或保留。一个 job 可以被多个 trigger 关联，但是一个 trigger 只能关联一个 job。[出处](https://www.ibm.com/developerworks/cn/opensource/os-cn-quartz/index.html)

***scheduler***

在 Quartz 中， scheduler 由 scheduler 工厂创建：DirectSchedulerFactory 或者 StdSchedulerFactory。 StdSchedulerFactory可以使用读取配置文件，因此更加简单也更常用。

![](https://www.ibm.com/developerworks/cn/opensource/os-cn-quartz/image001.gif)

### Job Stores

负责存储所有工作数据。 需要根据实际需要选择合适的JobStore。

***RAMJobStore***

数据保存在RAM,速度最快。但是程序结束时,数据会丢失。

***JDBC JobStore***

JDBCJobStore几乎可以与任何数据库一起使用，已被广泛应用于Oracle，PostgreSQL，MySQL，MS SQLServer，HSQLDB和DB2。



### 线程视图

在 Quartz 中，有两类线程，Scheduler 调度线程和任务执行线程，其中任务执行线程通常使用一个线程池维护一组线程。

图 2. Quartz 线程视图
![](https://www.ibm.com/developerworks/cn/opensource/os-cn-quartz/image002.gif)

Scheduler 调度线程主要有两个： 执行常规调度的线程，和执行 misfired trigger (错过的任务)的线程。常规调度线程轮询存储的所有 trigger，如果有需要触发的 trigger，即到达了下一次触发的时间，则从任务执行线程池获取一个空闲线程，执行与该 trigger 关联的任务。Misfire 线程是扫描所有的 trigger，查看是否有 misfired trigger，如果有的话根据 misfire 的策略分别处理。下图描述了这两个线程的基本流程：

![](https://www.ibm.com/developerworks/cn/opensource/os-cn-quartz/image003.png)

### cron表达式

顺序| 秒 | 分钟 | 小时| 日期 | 月份 | 星期 | 年（可选） 
---|---|---|---|---|---|---|---
取值 | 0-59 | 0-59 | 0-23 |1-30(31) | 1-12 | 1-7 |   
允许特殊字符 | , - * /| , - * / | , - * / | , - * / ? L W C| , - * /|, - * / L # C | 1970-2099 , - * /

> *：代表所有可能的值    
-：指定范围   
,：列出枚举  例如在分钟里，"5,15"表示5分钟和20分钟触发   
/：指定增量  例如在分钟里，"3/15"表示从3分钟开始，没隔15分钟执行一次   
?：表示没有具体的值，使用?要注意冲突   
L：表示last，例如星期中表示7或SAT，月份中表示最后一天31或30，6L表示这个月倒数第6天，FRIL表示这个月的最后一个星期五   
W：只能用在日期中，表示最接近指定天的工作日   
#：只能用在星期中，表示这个月的第几个周几，例如6#3表示这个月的第3个周五   


```
0 * * * * ? 每1分钟触发一次
0 0 * * * ? 每天每1小时触发一次
0 0 10 * * ? 每天10点触发一次
0 * 14 * * ? 在每天下午2点到下午2:59期间的每1分钟触发
0 30 9 1 * ? 每月1号上午9点半
0 15 10 15 * ? 每月15日上午10:15触发
*/5 * * * * ? 每隔5秒执行一次
0 */1 * * * ? 每隔1分钟执行一次
0 0 5-15 * * ? 每天5-15点整点触发
0 0/3 * * * ? 每三分钟触发一次
0 0 0 1 * ?  每月1号凌晨执行一次
```


## 参考资料

[官方文档地址](http://www.quartz-scheduler.org/documentation/)

[Spring Boot Quartz Scheduler Example: Building an Email Scheduling app](https://www.callicoder.com/spring-boot-quartz-scheduler-email-scheduling-example/)

[spring-boot-2.0.3之quartz集成，不是你想的那样哦！](https://www.cnblogs.com/youzhibing/p/10024558.html)

[spring-boot-2.0.3之quartz集成，数据源问题，源码探究](https://www.cnblogs.com/youzhibing/p/10056696.html)

[非官方翻译](https://www.w3cschool.cn/quartz_doc/quartz_doc-1xbu2clr.html)

[基于 Quartz 开发企业级任务调度应用](https://www.ibm.com/developerworks/cn/opensource/os-cn-quartz/index.html)

[在线cron表达式](http://cron.qqe2.com/)

[spring quartz](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-quartz.html)

<!-- [基于SpringBoot实现定时任务 ApplicationListener ](http://www.zhaoguojian.com/2018/07/16/%E5%9F%BA%E4%BA%8ESpringBoot%E5%AE%9E%E7%8E%B0%E5%AE%9A%E6%97%B6%E4%BB%BB%E5%8A%A1/) -->


