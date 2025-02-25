package com.formssi.job.snailjob;


import com.formssi.job.service.SyncUserOrgTaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 同步人事系统用户、部门信息的定时任务
 */
@Component
@RequiredArgsConstructor
public class SyncUserOrgTask {

    private final SyncUserOrgTaskService syncUserOrgTaskService;

    private static final Logger log = LoggerFactory.getLogger(SyncUserOrgTask.class);

    /**
     * 同步用户信息
     */
    @Scheduled(cron = "${schedule.syncUserCron}")
    public void syncUserInfo() {
        log.info("开始同步人事系统用户信息...");
        try {
            syncUserOrgTaskService.syncUserInfo();
        } catch (Exception e) {
            log.error("同步人事系统用户信息失败", e);
        }
    }

    /**
     * 同步组织信息
     */
//    @Scheduled(cron = "${schedule.syncOrgCron}")
    public void syncOrgInfo() {
        log.info("开始同步人事系统组织信息...");
        try {
            syncUserOrgTaskService.syncOrgInfo();
        } catch (Exception e) {
            log.error("同步人事系统组织信息失败", e);
        }
    }


    /**
     * 测试更换领用人
     */
//    @Scheduled(cron = "${schedule.syncOrgCron}")
//    public void syncOrgInfo() {
//        log.info("开始更换领用人...");
//        try {
//            syncUserOrgTaskService.testaaa();
//        } catch (Exception e) {
//            log.error("更换领用人失败", e);
//        }
//    }
}
