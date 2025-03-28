package com.formssi.job.snailjob;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.formssi.job.service.SyncUserOrgTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author opensnail
 * @date 2024-05-17
 * 同步OA系统用户机构 -> 资产系统
 */
@Slf4j
@Component
@JobExecutor(name = "syncAssetUserOrgJobExecutor")
public class SyncAssetUserOrgJobExecutor {
    @Autowired
    private SyncUserOrgTaskService syncUserOrgTaskService;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("syncAssetUserOrgJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        SnailJobLog.REMOTE.info("syncAssetUserOrgJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));

        log.info("开始同步组织信息：OA系统->资产系统...");
        try {
            syncUserOrgTaskService.syncOrgInfoToAsset();
        } catch (Exception e) {
            log.error("同步组织信息：OA系统->资产系统，失败", e);
        }

        log.info("开始同步用户信息：OA系统->资产系统...");
        try {
            syncUserOrgTaskService.syncUserInfoToAsset();
        } catch (Exception e) {
            log.error("同步用户信息：OA系统->资产系统，失败", e);
        }

        return ExecuteResult.success("同步资产系统用户、机构任务执行完成");
    }
}
