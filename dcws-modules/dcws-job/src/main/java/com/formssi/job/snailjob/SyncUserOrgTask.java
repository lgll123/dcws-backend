package com.formssi.job.snailjob;


import com.formssi.job.service.SyncUserOrgTaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SyncUserOrgTask {

    private final SyncUserOrgTaskService syncUserOrgTaskService;

    private static final Logger log = LoggerFactory.getLogger(SyncUserOrgTask.class);

    /**
     * 同步用户信息：人事系统->OA系统
     */
//    @Scheduled(cron = "${schedule.syncUserCron}")
    public void syncUserInfo() {
        log.info("开始同步用户信息：人事系统->OA系统...");
        try {
            syncUserOrgTaskService.syncUserInfo();
        } catch (Exception e) {
            log.error("同步用户信息：人事系统->OA系统，失败", e);
        }
    }

    /**
     * 同步组织信息：人事系统->OA系统
     */
//    @Scheduled(cron = "${schedule.syncOrgCron}")
    public void syncOrgInfo() {
        log.info("开始同步组织信息：人事系统->OA系统...");
        try {
            syncUserOrgTaskService.syncOrgInfo();
        } catch (Exception e) {
            log.error("同步组织信息：人事系统->OA系统，失败", e);
        }
    }

    /**
     * 同步组织信息：OA系统->资产系统
     */
//    @Scheduled(cron = "${schedule.syncAssetOrgCron}")
    public void syncOrgInfoToAsset() {
        log.info("开始同步组织信息：OA系统->资产系统...");
        try {
            syncUserOrgTaskService.syncOrgInfoToAsset();
        } catch (Exception e) {
            log.error("同步组织信息：OA系统->资产系统，失败", e);
        }
    }

    /**
     * 同步用户信息：OA系统->资产系统
     */
//    @Scheduled(cron = "${schedule.syncAssetUserCron}")
    public void syncUserInfoToAsset() {
        log.info("开始同步用户信息：OA系统->资产系统...");
        try {
            syncUserOrgTaskService.syncUserInfoToAsset();
        } catch (Exception e) {
            log.error("同步用户信息：OA系统->资产系统，失败", e);
        }
    }

}
