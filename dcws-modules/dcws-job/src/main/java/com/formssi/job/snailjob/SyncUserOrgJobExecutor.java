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
 * 同步人事系统用户机构 -> OA系统
 */
@Slf4j
@Component
@JobExecutor(name = "syncUserOrgJobExecutor")
public class SyncUserOrgJobExecutor {
    @Autowired
    private SyncUserOrgTaskService syncUserOrgTaskService;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("syncUserOrgJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        SnailJobLog.REMOTE.info("syncUserOrgJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));

        log.info("开始同步用户信息：人事系统->OA系统...");
        try {
            syncUserOrgTaskService.syncUserInfo();
        } catch (Exception e) {
            log.error("同步用户信息：人事系统->OA系统，失败", e);
            SnailJobLog.LOCAL.info("syncUserOrgJobExecutor. exception:{}", e.getMessage());
        }

        log.info("开始同步组织信息：人事系统->OA系统...");
        try {
//            syncUserOrgTaskService.syncOrgInfo(); TODO 先屏幕 否则影响部门领导人数据
        } catch (Exception e) {
            log.error("同步组织信息：人事系统->OA系统，失败", e);
            SnailJobLog.LOCAL.info("syncUserOrgJobExecutor. exception:{}", e.getMessage());
        }

        return ExecuteResult.success("同步人事系统用户、机构任务执行完成");
    }
}
