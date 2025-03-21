package com.formssi.workflow.externalsystem.assets.servicewrapper;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.workflow.domain.DcwsProjectTask;
import com.formssi.workflow.domain.bo.CompleteTaskBo;
import com.formssi.workflow.domain.bo.DcwsProjectBo;
import com.formssi.workflow.domain.bo.StartProcessBo;
import com.formssi.workflow.domain.vo.DcwsProjectVo;
import com.formssi.workflow.mapper.DcwsProjectTaskMapper;
import com.formssi.workflow.service.IActTaskService;
import com.formssi.workflow.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IActTaskServiceWrapper {
    private final IActTaskService actTaskService;  // 原有服务（含@Transactional的方法）
    private final ProjectManagementService projectManagementService;  // 原有服务（含@Transactional的方法）
    private final TransactionTemplate transactionTemplate;  // 编程式事务模板
    private final DcwsProjectTaskMapper dcwsProjectTaskMapper;

    /**
     * 在新事务中调用原方法，异常仅回滚新事务
     * 启动流程
     */
    public Map<String, Object> startProcessInNewTransaction(StartProcessBo startProcessBo) {
        return transactionTemplate.execute(status -> {
            try {
                return actTaskService.startWorkFlow(startProcessBo);  // 调用原方法（已注解@Transactional）
            } catch (Exception e) {
                status.setRollbackOnly();  // 标记事务回滚
                // 记录日志，但无需处理（新事务已标记回滚）
                // 记录失败记录，待auto处理启动流程 TODO
                log.info("独立流程启动失败: " + e.getMessage());
                return null;  // 返回值
            }
        });
    }

    /**
     * 在新事务中调用原方法，异常仅回滚新事务
     * 完成任务
     */
    public boolean completeTaskInNewTransaction(CompleteTaskBo completeTaskBo) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                return actTaskService.completeTask(completeTaskBo);  // 调用原方法（已注解@Transactional）
            } catch (Exception e) {
                status.setRollbackOnly();  // 标记事务回滚
                // 记录日志，但无需处理（新事务已标记回滚）
                // 记录失败记录，待auto处理启动流程 TODO
                log.info("独立流程完成任务失败: " + e.getMessage());
                return false;  // 返回值
            }
        }));
    }

    /**
     * 在新事务中调用原方法，异常仅回滚新事务
     * 启动项目
     */
    public Integer startProjectInNewTransaction(DcwsProjectBo projectBo, Map<String,Object> taskNodeData) {
        return transactionTemplate.execute(status -> {
            try {
                DcwsProjectVo dcwsProjectVo = projectManagementService.insertByBo(projectBo);// 调用原方法（已注解@Transactional）
                DcwsProjectTask dcwsProjectTask = new DcwsProjectTask();
                dcwsProjectTask.setTaskType(Convert.toStr(taskNodeData.get("applyType")));
                dcwsProjectTask.setProjectId(dcwsProjectVo.getProjectId());
                dcwsProjectTask.setTaskName("服务申请");
                dcwsProjectTask.setTaskStatus("finish");
                dcwsProjectTask.setCreateBy(Convert.toLong(taskNodeData.get("applicantId")));
                dcwsProjectTask.setCreateTime(DateUtil.parse(Convert.toStr(taskNodeData.get("applyDate"))));
                dcwsProjectTask.setCreateEmpName(Convert.toStr(taskNodeData.get("applicant")));
                dcwsProjectTask.setBusinessKey(Convert.toStr(taskNodeData.get("id")));
               return dcwsProjectTaskMapper.insert(dcwsProjectTask);
            } catch (Exception e) {
                status.setRollbackOnly();  // 标记事务回滚
                // 记录日志，但无需处理（新事务已标记回滚）
                // 记录失败记录，待auto处理启动流程 TODO
                log.info("项目启动失败: " + e.getMessage());
            }
            return 1;
        });
    }
}
