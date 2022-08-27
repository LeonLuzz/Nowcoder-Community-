package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 删除Quartz数据库内容
@SpringBootTest
public class QuartzTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDeleteJob(){
        try {
            boolean res = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
            System.out.println(res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

}
