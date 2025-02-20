package com.formssi.workflow.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.domain.vo.TaskVo;
import com.formssi.workflow.domain.vo.VariableVo;

import com.formssi.workflow.domain.vo.WfFormPermissionVo;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 任务 服务层
 *
 * @author may
 */
public interface IActTaskService {
    /**
     * 启动任务
     *
     * @param startProcessBo 启动流程参数
     * @return 结果
     */
    Map<String, Object> startWorkFlow(StartProcessBo startProcessBo);


    /**
     * 办理任务
     *
     * @param completeTaskBo 办理任务参数
     * @return 结果
     */
    boolean completeTask(CompleteTaskBo completeTaskBo);

    /**
     * 查询当前用户的待办任务
     *
     * @param taskBo    参数
     * @param pageQuery 分页
     * @return 结果
     */
    TableDataInfo<TaskVo> getPageByTaskWait(TaskBo taskBo, PageQuery pageQuery);

    /**
     * 查询当前租户所有待办任务
     *
     * @param taskBo    参数
     * @param pageQuery 分页
     * @return 结果
     */
    TableDataInfo<TaskVo> getPageByAllTaskWait(TaskBo taskBo, PageQuery pageQuery);


    /**
     * 查询当前用户的已办任务
     *
     * @param taskBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<TaskVo> getPageByTaskFinish(TaskBo taskBo, PageQuery pageQuery);

    /**
     * 查询当前用户的抄送
     *
     * @param taskBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<TaskVo> getPageByTaskCopy(TaskBo taskBo, PageQuery pageQuery);

    /**
     * 查询当前租户所有已办任务
     *
     * @param taskBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<TaskVo> getPageByAllTaskFinish(TaskBo taskBo, PageQuery pageQuery);

    /**
     * 委派任务
     *
     * @param delegateBo 参数
     * @return 结果
     */
    boolean delegateTask(DelegateBo delegateBo);

    /**
     * 终止任务
     *
     * @param terminationBo 参数
     * @return 结果
     */
    boolean terminationTask(TerminationBo terminationBo);

    /**
     * 转办任务
     *
     * @param transmitBo 参数
     * @return 结果
     */
    boolean transferTask(TransmitBo transmitBo);

    /**
     * 会签任务加签
     *
     * @param addMultiBo 参数
     * @return 结果
     */
    boolean addMultiInstanceExecution(AddMultiBo addMultiBo);

    /**
     * 会签任务减签
     *
     * @param deleteMultiBo 参数
     * @return 结果
     */
    boolean deleteMultiInstanceExecution(DeleteMultiBo deleteMultiBo);

    /**
     * 检查撤回按钮
     * @param rollbackProcessBo 参数
     * @return 结果
     */
    boolean checkRollback(RollbackProcessBo rollbackProcessBo);

    /**
     * 撤回任务
     * @param rollbackProcessBo 参数
     * @return 结果
     */
    boolean rollbackTask(RollbackProcessBo rollbackProcessBo);

    /**
     * 驳回审批
     *
     * @param backProcessBo 参数
     * @return 流程实例id
     */
    String backProcess(BackProcessBo backProcessBo);

    /**
     * 获取表单权限
     * @param formStatusBo 表单状态
     * @return 表单权限
     */
    List<WfFormPermissionVo> getFormPermission(FormStatusBo formStatusBo);

    /**
     * 修改任务办理人
     *
     * @param taskIds 任务id
     * @param userId  办理人id
     * @return 结果
     */
    boolean updateAssignee(String[] taskIds, String userId);

    /**
     * 查询流程变量
     *
     * @param taskId 任务id
     * @return 结果
     */
    List<VariableVo> getInstanceVariable(String taskId);

    /**
     * 查询工作流任务用户选择加签人员
     *
     * @param taskId 任务id
     * @return 结果
     */
    String getTaskUserIdsByAddMultiInstance(String taskId);

    /**
     * 查询工作流选择减签人员
     *
     * @param taskId 任务id
     * @return 结果
     */
    List<TaskVo> getListByDeleteMultiInstance(String taskId);

    /**
     * 获取下一个节点 add by yqh
     *
     * @param nextNodeBo
     * @return 结果
     */
    Map<String, Object> getNextNodeInfo(NextNodeBo nextNodeBo);
}
