package com.formssi.workflow.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.ISysUserService;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.service.CommonApproveService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.WfTaskBackNode;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.service.IActTaskService;
import com.formssi.workflow.service.IWfTaskBackNodeService;
import com.formssi.workflow.utils.QueryUtils;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 任务管理 控制层
 *
 * @author may
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/task")
public class ActTaskController extends BaseController {

    @Autowired(required = false)
    private TaskService taskService;
    private final IActTaskService actTaskService;
    private final IWfTaskBackNodeService wfTaskBackNodeService;
    private final CommonApproveService commonApproveService;
    private final ISysUserService iSysUserService;


    /**
     * 启动任务
     *
     * @param startProcessBo 启动流程参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/startWorkFlow")
    public R<Map<String, Object>> startWorkFlow(@Validated(AddGroup.class) @RequestBody StartProcessBo startProcessBo) {
        Map<String, Object> map = actTaskService.startWorkFlow(startProcessBo);
        return R.ok("提交成功", map);
    }

    /**
     * 办理任务
     *
     * @param completeTaskBo 办理任务参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/completeTask")
    public R<Void> completeTask(@Validated(AddGroup.class) @RequestBody CompleteTaskBo completeTaskBo) {
        return toAjax(actTaskService.completeTask(completeTaskBo));
    }

    /**
     * 查询当前用户的待办任务
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByTaskWait")
    public TableDataInfo<TaskVo> getPageByTaskWait(TaskBo taskBo, PageQuery pageQuery) {
        if ("1".equals(taskBo.getWfType())){
            return actTaskService.getPageByTaskWait(taskBo, pageQuery);
        }else {
            TableDataInfo<DcwsApproveVo> dcwsList = commonApproveService.getPageByTaskWait(new DcwsApproveBo(), pageQuery);
            List<DcwsApproveVo> list = dcwsList.getRows();
            List<TaskVo> listTemp = new ArrayList<>();
            TableDataInfo<TaskVo> build = TableDataInfo.build();
            if (CollUtil.isNotEmpty(list)){
                for (DcwsApproveVo dcwsApproveVo : list){
                    TaskVo taskVo = new TaskVo();
                    taskVo.setProcessDefinitionName("非标准流程");
                    taskVo.setBusinessStatus(dcwsApproveVo.getStatus());
                    taskVo.setStartTime(dcwsApproveVo.getCreateTime());
                    taskVo.setName(dcwsApproveVo.getTaskName());
                    SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(dcwsApproveVo.getUserId()));
                    ParticipantVo participantVo = new ParticipantVo();
                    participantVo.setCandidate(Arrays.asList(Long.valueOf(dcwsApproveVo.getUserId())));
                    participantVo.setCandidateName(Arrays.asList(sysUserVo.getUserName()));
                    taskVo.setParticipantVo(participantVo);
                    listTemp.add(taskVo);
                }
            }
            build.setRows(listTemp);
            build.setTotal(dcwsList.getTotal());
            return build;
        }

    }

    /**
     * 查询当前租户所有待办任务
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByAllTaskWait")
    public TableDataInfo<TaskVo> getPageByAllTaskWait(TaskBo taskBo, PageQuery pageQuery) {
        return actTaskService.getPageByAllTaskWait(taskBo, pageQuery);
    }

    /**
     * 查询当前用户的已办任务
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByTaskFinish")
    public TableDataInfo<TaskVo> getPageByTaskFinish(TaskBo taskBo, PageQuery pageQuery) {
        if ("1".equals(taskBo.getWfType())){
            return actTaskService.getPageByTaskFinish(taskBo, pageQuery);
        }else {
            TableDataInfo<DcwsApproveVo> dcwsList = commonApproveService.getPageByTaskFinish(new DcwsApproveBo(), pageQuery);
            List<DcwsApproveVo> list = dcwsList.getRows();
            List<TaskVo> listTemp = new ArrayList<>();
            TableDataInfo<TaskVo> build = TableDataInfo.build();
            if (CollUtil.isNotEmpty(list)){
                for (DcwsApproveVo dcwsApproveVo : list){
                    TaskVo taskVo = new TaskVo();
                    taskVo.setProcessDefinitionName("非标准流程");
                    taskVo.setBusinessStatus(dcwsApproveVo.getStatus());
                    taskVo.setStartTime(dcwsApproveVo.getCreateTime());
                    taskVo.setName(dcwsApproveVo.getTaskName());
                    SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(dcwsApproveVo.getUserId()));
                    taskVo.setAssignee(Long.valueOf(dcwsApproveVo.getUserId()));
                    taskVo.setAssigneeName(sysUserVo.getUserName());
                    listTemp.add(taskVo);
                }
            }
            build.setRows(listTemp);
            build.setTotal(dcwsList.getTotal());
            return build;
        }
    }

    /**
     * 查询当前用户的抄送
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByTaskCopy")
    public TableDataInfo<TaskVo> getPageByTaskCopy(TaskBo taskBo, PageQuery pageQuery) {
        return actTaskService.getPageByTaskCopy(taskBo, pageQuery);
    }

    /**
     * 查询当前租户所有已办任务
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByAllTaskFinish")
    public TableDataInfo<TaskVo> getPageByAllTaskFinish(TaskBo taskBo, PageQuery pageQuery) {
        return actTaskService.getPageByAllTaskFinish(taskBo, pageQuery);
    }

    /**
     * 签收（拾取）任务
     *
     * @param taskId 任务id
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/claim/{taskId}")
    public R<Void> claimTask(@NotBlank(message = "任务id不能为空") @PathVariable String taskId) {
        try {
            taskService.claim(taskId, Convert.toStr(LoginHelper.getUserId()));
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("签收任务失败：" + e.getMessage());
        }
    }

    /**
     * 归还（拾取的）任务
     *
     * @param taskId 任务id
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/returnTask/{taskId}")
    public R<Void> returnTask(@NotBlank(message = "任务id不能为空") @PathVariable String taskId) {
        try {
            taskService.setAssignee(taskId, null);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("归还任务失败：" + e.getMessage());
        }
    }

    /**
     * 委派任务
     *
     * @param delegateBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/delegateTask")
    public R<Void> delegateTask(@Validated({AddGroup.class}) @RequestBody DelegateBo delegateBo) {
        return toAjax(actTaskService.delegateTask(delegateBo));
    }

    /**
     * 终止任务
     *
     * @param terminationBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @PostMapping("/terminationTask")
    public R<Void> terminationTask(@RequestBody TerminationBo terminationBo) {
        return toAjax(actTaskService.terminationTask(terminationBo));
    }

    /**
     * 转办任务
     *
     * @param transmitBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/transferTask")
    public R<Void> transferTask(@Validated({AddGroup.class}) @RequestBody TransmitBo transmitBo) {
        return toAjax(actTaskService.transferTask(transmitBo));
    }

    /**
     * 会签任务加签
     *
     * @param addMultiBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addMultiInstanceExecution")
    public R<Void> addMultiInstanceExecution(@Validated({AddGroup.class}) @RequestBody AddMultiBo addMultiBo) {
        return toAjax(actTaskService.addMultiInstanceExecution(addMultiBo));
    }

    /**
     * 会签任务减签
     *
     * @param deleteMultiBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/deleteMultiInstanceExecution")
    public R<Void> deleteMultiInstanceExecution(@Validated({AddGroup.class}) @RequestBody DeleteMultiBo deleteMultiBo) {
        return toAjax(actTaskService.deleteMultiInstanceExecution(deleteMultiBo));
    }

    /**
     * 检查撤回按钮
     *
     * @param rollbackProcessBo 参数
     */
    @Log(title = "任务管理")
    @RepeatSubmit()
    @PostMapping("/checkRollback")
    public R<Boolean> checkRollback(@Validated({AddGroup.class}) @RequestBody RollbackProcessBo rollbackProcessBo) {
        return R.ok(actTaskService.checkRollback(rollbackProcessBo));
    }

    /**
     * 撤回任务
     *
     * @param rollbackProcessBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/rollbackTask")
    public R<Void> rollbackTask(@Validated({AddGroup.class}) @RequestBody RollbackProcessBo rollbackProcessBo) {
        return toAjax(actTaskService.rollbackTask(rollbackProcessBo));
    }

    /**
     * 驳回审批
     *
     * @param backProcessBo 参数
     */
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/backProcess")
    public R<String> backProcess(@Validated({AddGroup.class}) @RequestBody BackProcessBo backProcessBo) {
        return R.ok("操作成功", actTaskService.backProcess(backProcessBo));
    }

    /**
     * 获取当前任务
     *
     * @param taskId 任务id
     */
    @GetMapping("/getTaskById/{taskId}")
    public R<TaskVo> getTaskById(@PathVariable String taskId) {
        return R.ok(QueryUtils.getTask(taskId));
    }

    /**
     * 获取表单权限
     * @param formStatusBo 表单状态
     */
    @PostMapping("/getFormPermission")
    public R<List<WfFormPermissionVo>> getFormPermission(@RequestBody FormStatusBo formStatusBo) {
        return R.ok(actTaskService.getFormPermission(formStatusBo));
    }


    /**
     * 修改任务办理人
     *
     * @param taskIds 任务id
     * @param userId  办理人id
     */
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateAssignee/{taskIds}/{userId}")
    public R<Void> updateAssignee(@PathVariable String[] taskIds, @PathVariable String userId) {
        return toAjax(actTaskService.updateAssignee(taskIds, userId));
    }

    /**
     * 查询流程变量
     *
     * @param taskId 任务id
     */
    @GetMapping("/getInstanceVariable/{taskId}")
    public R<List<VariableVo>> getProcessInstVariable(@PathVariable String taskId) {
        return R.ok(actTaskService.getInstanceVariable(taskId));
    }

    /**
     * 获取可驳回得任务节点
     *
     * @param processInstanceId 流程实例id
     */
    @GetMapping("/getTaskNodeList/{processInstanceId}")
    public R<List<WfTaskBackNode>> getNodeList(@PathVariable String processInstanceId) {
        return R.ok(CollUtil.reverse(wfTaskBackNodeService.getListByInstanceId(processInstanceId)));
    }

    /**
     * 查询工作流任务用户选择加签人员
     *
     * @param taskId 任务id
     */
    @GetMapping("/getTaskUserIdsByAddMultiInstance/{taskId}")
    public R<String> getTaskUserIdsByAddMultiInstance(@PathVariable String taskId) {
        return R.ok("操作成功", actTaskService.getTaskUserIdsByAddMultiInstance(taskId));
    }

    /**
     * 查询工作流选择减签人员
     *
     * @param taskId 任务id
     */
    @GetMapping("/getListByDeleteMultiInstance/{taskId}")
    public R<List<TaskVo>> getListByDeleteMultiInstance(@PathVariable String taskId) {
        return R.ok(actTaskService.getListByDeleteMultiInstance(taskId));
    }

}
