package com.formssi.workflow.controller;

import cn.hutool.core.collection.CollUtil;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.workflow.utils.DcwsDateUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.service.DcwsIActProcessInstanceService;
import com.formssi.workflow.service.NormalTaskService;
import lombok.RequiredArgsConstructor;
import com.formssi.common.core.domain.R;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 流程实例管理 控制层
 *
 * @author may
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/processInstancedcws")
public class DcwsActProcessInstanceController extends BaseController {

    private final DcwsIActProcessInstanceService dcwsIActProcessInstanceService;
    private final NormalTaskService normalTaskService;

    /**
     * 获取审批记录
     *
     */
    @PostMapping("/getHistoryRecord")
    public R<List<DcwsActHistoryInfoVo>> getHistoryRecord(@RequestBody DcwsProcessInstanceBo processInstanceBo) {
        if ("20".equals(processInstanceBo.getWfType())){
            DcwsNormalTaskVo dcwsApproveVo = normalTaskService.queryById(processInstanceBo.getKey());
            if (StringUtils.isNotEmpty(dcwsApproveVo.getBusinessKey())){
                //标准流程中新增的非标准流程，审批记录需合并
                List<DcwsActHistoryInfoVo> list = dcwsIActProcessInstanceService.getHistoryRecord(dcwsApproveVo.getBusinessKey());
                List<DcwsNormalTaskHandleHisVo> commonList = normalTaskService.getHistoryRecord(processInstanceBo.getKey());
                for (DcwsNormalTaskHandleHisVo dcwsHisVo : commonList){
                    DcwsActHistoryInfoVo actHistoryInfoVo = new DcwsActHistoryInfoVo();
                    actHistoryInfoVo.setName(dcwsApproveVo.getTaskName());
                    actHistoryInfoVo.setAssignee(String.valueOf(dcwsHisVo.getUserId()));
                    actHistoryInfoVo.setUserName(dcwsHisVo.getUserName());
                    actHistoryInfoVo.setStatus(dcwsHisVo.getStatus());
                    actHistoryInfoVo.setStatusName(BusinessStatusEnum.findByStatus(dcwsHisVo.getStatus()));
                    if("pass".equals(dcwsHisVo.getStatus())){
                        actHistoryInfoVo.setStatusName("通过");
                    }
                    actHistoryInfoVo.setComment(dcwsHisVo.getComment());
                    actHistoryInfoVo.setStartTime(dcwsHisVo.getCreateTime());
                    actHistoryInfoVo.setEndTime(dcwsHisVo.getUpdateTime());
                    if (!Objects.isNull(dcwsHisVo.getCreateTime()) && !Objects.isNull(dcwsHisVo.getUpdateTime())){
                        actHistoryInfoVo.setRunDuration(DcwsDateUtils.getDatePoor(dcwsHisVo.getCreateTime(),dcwsHisVo.getUpdateTime()));
                    }
                    list.add(actHistoryInfoVo);
                }
                list = StreamUtils.sorted(list, Comparator.comparing(DcwsActHistoryInfoVo::getStartTime, Comparator.nullsFirst(Date::compareTo)).reversed());
                return R.ok(list);
            }else {
                //标准流程外新增的非标准流程，单独显示审批记录
                List<DcwsNormalTaskHandleHisVo> list = normalTaskService.getHistoryRecord(processInstanceBo.getKey());
                List<DcwsActHistoryInfoVo> tempList = new ArrayList<>();
                for (DcwsNormalTaskHandleHisVo dcwsHisVo : list){
                    DcwsActHistoryInfoVo actHistoryInfoVo = new DcwsActHistoryInfoVo();
                    actHistoryInfoVo.setName(dcwsApproveVo.getTaskName());
                    actHistoryInfoVo.setUserName(dcwsHisVo.getUserName());
                    actHistoryInfoVo.setAssignee(String.valueOf(dcwsHisVo.getUserId()));
                    actHistoryInfoVo.setStatus(dcwsHisVo.getStatus());
                    actHistoryInfoVo.setStatusName(BusinessStatusEnum.findByStatus(dcwsHisVo.getStatus()));
                    if("pass".equals(dcwsHisVo.getStatus())){
                        actHistoryInfoVo.setStatusName("通过");
                    }
                    actHistoryInfoVo.setComment(dcwsHisVo.getComment());
                    actHistoryInfoVo.setStartTime(dcwsHisVo.getCreateTime());
                    actHistoryInfoVo.setEndTime(dcwsHisVo.getUpdateTime());
                    if (!Objects.isNull(dcwsHisVo.getCreateTime()) && !Objects.isNull(dcwsHisVo.getUpdateTime())){
                        actHistoryInfoVo.setRunDuration(DcwsDateUtils.getDatePoor(dcwsHisVo.getCreateTime(),dcwsHisVo.getUpdateTime()));
                    }
                    tempList.add(actHistoryInfoVo);
                }
                return R.ok(tempList);
            }
        }else {
            return R.ok(dcwsIActProcessInstanceService.getHistoryRecord(processInstanceBo.getBusinessKey()));
        }
    }

    /**
     * 分页查询当前登录人单据
     *
     * @param bo 参数
     */
    @GetMapping("/getPageByCurrent")
    public TableDataInfo<DcwsProcessInstanceVo> getPageByCurrent(DcwsProcessInstanceBo bo, PageQuery pageQuery) {
        if (!"20".equals(bo.getWfType())){
            return dcwsIActProcessInstanceService.getPageByCurrent(bo, pageQuery);
        }else {
            DcwsNormalTaskBo dcwsNormalTaskBo = new DcwsNormalTaskBo();
            if(StringUtils.isNotEmpty(bo.getBusinessKey())){
                dcwsNormalTaskBo.setTaskId(bo.getBusinessKey());
            }
            if(!Objects.isNull(bo.getStartTime())){
                dcwsNormalTaskBo.setStartTime(bo.getStartTime());
            }
            if(!Objects.isNull(bo.getEndTime())){
                dcwsNormalTaskBo.setEndTime(bo.getEndTime());
            }
            TableDataInfo<DcwsNormalTaskVo> dcwsList = normalTaskService.queryPageList(dcwsNormalTaskBo, pageQuery);
            List<DcwsNormalTaskVo> list = dcwsList.getRows();
            List<DcwsProcessInstanceVo> listTemp = new ArrayList<>();
            TableDataInfo<DcwsProcessInstanceVo> build = TableDataInfo.build();
            if (CollUtil.isNotEmpty(list)){
                for (DcwsNormalTaskVo dcwsNormalTaskVo : list){
                    DcwsProcessInstanceVo processInstanceVo = new DcwsProcessInstanceVo();
                    processInstanceVo.setProcessDefinitionName("非标准流程");
                    processInstanceVo.setBusinessStatus(dcwsNormalTaskVo.getStatus());
                    processInstanceVo.setBusinessStatusName(BusinessStatusEnum.findByStatus(dcwsNormalTaskVo.getStatus()));
                    processInstanceVo.setStartTime(dcwsNormalTaskVo.getCreateTime());
                    processInstanceVo.setWfType("20");
                    processInstanceVo.setId(String.valueOf(dcwsNormalTaskVo.getTaskId()));
                    processInstanceVo.setBusinessKey(String.valueOf(dcwsNormalTaskVo.getTaskId()));
                    processInstanceVo.setApplyReason(dcwsNormalTaskVo.getRemark());
                    processInstanceVo.setName(dcwsNormalTaskVo.getTaskName());
                    listTemp.add(processInstanceVo);
                }
            }
            build.setRows(listTemp);
            build.setTotal(dcwsList.getTotal());
            return build;
        }
    }
}
