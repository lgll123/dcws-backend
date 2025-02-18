package com.formssi.workflow.controller;

import cn.hutool.core.collection.CollUtil;
import com.formssi.workflow.domain.bo.DcwsApproveBo;
import com.formssi.workflow.domain.vo.DcwsApproveVo;
import com.formssi.workflow.service.CommonApproveService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.ProcessInstanceBo;
import com.formssi.workflow.domain.bo.ProcessInvalidBo;
import com.formssi.workflow.domain.bo.TaskUrgingBo;
import com.formssi.workflow.domain.vo.ActHistoryInfoVo;
import com.formssi.workflow.domain.vo.ProcessInstanceVo;
import com.formssi.workflow.service.IActProcessInstanceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 流程实例管理 控制层
 *
 * @author may
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/processInstance")
public class ActProcessInstanceController extends BaseController {

    private final IActProcessInstanceService actProcessInstanceService;
    private final CommonApproveService commonApproveService;

    /**
     * 分页查询正在运行的流程实例
     *
     * @param bo 参数
     */
    @GetMapping("/getPageByRunning")
    public TableDataInfo<ProcessInstanceVo> getPageByRunning(ProcessInstanceBo bo, PageQuery pageQuery) {
        return actProcessInstanceService.getPageByRunning(bo, pageQuery);
    }

    /**
     * 分页查询已结束的流程实例
     *
     * @param bo 参数
     */
    @GetMapping("/getPageByFinish")
    public TableDataInfo<ProcessInstanceVo> getPageByFinish(ProcessInstanceBo bo, PageQuery pageQuery) {
        return actProcessInstanceService.getPageByFinish(bo, pageQuery);
    }

    /**
     * 通过业务id获取历史流程图
     *
     * @param businessKey 业务id
     */
    @GetMapping("/getHistoryImage/{businessKey}")
    public R<String> getHistoryImage(@NotBlank(message = "业务id不能为空") @PathVariable String businessKey) {
        return R.ok("操作成功", actProcessInstanceService.getHistoryImage(businessKey));
    }

    /**
     * 通过业务id获取历史流程图运行中，历史等节点
     *
     * @param businessKey 业务id
     */
    @GetMapping("/getHistoryList/{businessKey}")
    public R<Map<String, Object>> getHistoryList(@NotBlank(message = "业务id不能为空") @PathVariable String businessKey) {
        return R.ok("操作成功", actProcessInstanceService.getHistoryList(businessKey));
    }

    /**
     * 获取审批记录
     *
     * @param businessKey 业务id
     */
    @GetMapping("/getHistoryRecord/{businessKey}")
    public R<List<ActHistoryInfoVo>> getHistoryRecord(@NotBlank(message = "业务id不能为空") @PathVariable String businessKey) {
        return R.ok(actProcessInstanceService.getHistoryRecord(businessKey));
    }

    /**
     * 作废流程实例，不会删除历史记录(删除运行中的实例)
     *
     * @param processInvalidBo 参数
     */
    @Log(title = "流程实例管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @PostMapping("/deleteRunInstance")
    public R<Void> deleteRunInstance(@Validated(AddGroup.class) @RequestBody ProcessInvalidBo processInvalidBo) {
        return toAjax(actProcessInstanceService.deleteRunInstance(processInvalidBo));
    }

    /**
     * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     *
     * @param businessKeys 业务id
     */
    @Log(title = "流程实例管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/deleteRunAndHisInstance/{businessKeys}")
    public R<Void> deleteRunAndHisInstance(@NotNull(message = "业务id不能为空") @PathVariable String[] businessKeys) {
        return toAjax(actProcessInstanceService.deleteRunAndHisInstance(Arrays.asList(businessKeys)));
    }

    /**
     * 已完成的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     *
     * @param businessKeys 业务id
     */
    @Log(title = "流程实例管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/deleteFinishAndHisInstance/{businessKeys}")
    public R<Void> deleteFinishAndHisInstance(@NotNull(message = "业务id不能为空") @PathVariable String[] businessKeys) {
        return toAjax(actProcessInstanceService.deleteFinishAndHisInstance(Arrays.asList(businessKeys)));
    }

    /**
     * 撤销流程申请
     *
     * @param businessKey 业务id
     */
    @Log(title = "流程实例管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/cancelProcessApply/{businessKey}")
    public R<Void> cancelProcessApply(@NotBlank(message = "业务id不能为空") @PathVariable String businessKey) {
        return toAjax(actProcessInstanceService.cancelProcessApply(businessKey));
    }

    /**
     * 分页查询当前登录人单据
     *
     * @param bo 参数
     */
    @GetMapping("/getPageByCurrent")
    public TableDataInfo<ProcessInstanceVo> getPageByCurrent(ProcessInstanceBo bo, PageQuery pageQuery) {
        if ("1".equals(bo.getWfType())){
            return actProcessInstanceService.getPageByCurrent(bo, pageQuery);
        }else {
            TableDataInfo<DcwsApproveVo> dcwsList = commonApproveService.queryPageList(new DcwsApproveBo(), pageQuery);
            List<DcwsApproveVo> list = dcwsList.getRows();
            List<ProcessInstanceVo> listTemp = new ArrayList<>();
            TableDataInfo<ProcessInstanceVo> build = TableDataInfo.build();
            if (CollUtil.isNotEmpty(list)){
                for (DcwsApproveVo dcwsApproveVo : list){
                    ProcessInstanceVo processInstanceVo = new ProcessInstanceVo();
                    processInstanceVo.setProcessDefinitionName("非标准流程");
                    processInstanceVo.setBusinessStatus(dcwsApproveVo.getStatus());
                    processInstanceVo.setStartTime(dcwsApproveVo.getCreateTime());
                    listTemp.add(processInstanceVo);
                }
            }
            build.setRows(listTemp);
            build.setTotal(dcwsList.getTotal());
            return build;
        }
    }

    /**
     * 任务催办(给当前任务办理人发送站内信，邮件，短信等)
     *
     * @param taskUrgingBo 任务催办
     */
    @Log(title = "流程实例管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/taskUrging")
    public R<Void> taskUrging(@RequestBody TaskUrgingBo taskUrgingBo) {
        return toAjax(actProcessInstanceService.taskUrging(taskUrgingBo));
    }

}
