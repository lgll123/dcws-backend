package com.formssi.workflow.controller;

import cn.hutool.core.collection.CollUtil;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.ISysUserService;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.service.NormalTaskService;
import lombok.RequiredArgsConstructor;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.service.DcwsIActTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 任务管理 控制层
 *
 * @author may
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/taskdcws")
public class DcwsActTaskController extends BaseController {

    private final DcwsIActTaskService actTaskService;
    private final NormalTaskService normalTaskService;
    private final ISysUserService iSysUserService;

    /**
     * 查询当前用户的待办任务
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByTaskWait")
    public TableDataInfo<DcwsTaskVo> getPageByTaskWait(DcwsTaskBo taskBo, PageQuery pageQuery) {
        if (!"20".equals(taskBo.getWfType())){
            return actTaskService.getPageByTaskWait(taskBo, pageQuery);
        }else {
            DcwsNormalTaskBo dcwsNormalTaskBo = new DcwsNormalTaskBo();
            dcwsNormalTaskBo.setTaskName(taskBo.getName());
            if(StringUtils.isNotEmpty(taskBo.getBusinessKey())){
                dcwsNormalTaskBo.setTaskId(taskBo.getBusinessKey());
            }
            TableDataInfo<DcwsNormalTaskVo> dcwsList = normalTaskService.getPageByTaskWait(dcwsNormalTaskBo, pageQuery);
            List<DcwsNormalTaskVo> list = dcwsList.getRows();
            List<DcwsTaskVo> listTemp = new ArrayList<>();
            TableDataInfo<DcwsTaskVo> build = TableDataInfo.build();
            if (CollUtil.isNotEmpty(list)){
                for (DcwsNormalTaskVo dcwsNormalTaskVo : list){
                    DcwsTaskVo taskVo = new DcwsTaskVo();
                    taskVo.setProcessDefinitionName("非标准流程");
                    taskVo.setName(dcwsNormalTaskVo.getTaskName());
                    taskVo.setBusinessStatus(dcwsNormalTaskVo.getStatus());
                    taskVo.setCreateTime(dcwsNormalTaskVo.getCreateTime());
                    taskVo.setName(dcwsNormalTaskVo.getTaskName());
                    SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(dcwsNormalTaskVo.getUserId()));
                    ParticipantVo participantVo = new ParticipantVo();
                    participantVo.setCandidate(Arrays.asList(Long.valueOf(dcwsNormalTaskVo.getUserId())));
                    participantVo.setCandidateName(Arrays.asList(sysUserVo.getUserName()));
                    taskVo.setParticipantVo(participantVo);
                    taskVo.setWfType("20");
                    taskVo.setId(String.valueOf(dcwsNormalTaskVo.getTaskId()));
                    taskVo.setBusinessKey(String.valueOf(dcwsNormalTaskVo.getTaskId()));
                    taskVo.setApplyReason(dcwsNormalTaskVo.getRemark());
                    listTemp.add(taskVo);
                }
            }
            build.setRows(listTemp);
            build.setTotal(dcwsList.getTotal());
            return build;
        }
    }


    /**
     * 查询当前用户的已办任务
     *
     * @param taskBo 参数
     */
    @GetMapping("/getPageByTaskFinish")
    public TableDataInfo<DcwsTaskVo> getPageByTaskFinish(DcwsTaskBo taskBo, PageQuery pageQuery) {
        if (!"20".equals(taskBo.getWfType())){
            return actTaskService.getPageByTaskFinish(taskBo, pageQuery);
        }else {
            DcwsNormalTaskBo dcwsNormalTaskBo = new DcwsNormalTaskBo();
            dcwsNormalTaskBo.setTaskName(taskBo.getName());
            if(StringUtils.isNotEmpty(taskBo.getBusinessKey())){
                dcwsNormalTaskBo.setTaskId(taskBo.getBusinessKey());
            }
            TableDataInfo<DcwsNormalTaskVo> dcwsList = normalTaskService.getPageByTaskFinish(dcwsNormalTaskBo, pageQuery);
            List<DcwsNormalTaskVo> list = dcwsList.getRows();
            List<DcwsTaskVo> listTemp = new ArrayList<>();
            TableDataInfo<DcwsTaskVo> build = TableDataInfo.build();
            if (CollUtil.isNotEmpty(list)){
                for (DcwsNormalTaskVo dcwsNormalTaskVo : list){
                    DcwsTaskVo taskVo = new DcwsTaskVo();
                    taskVo.setProcessDefinitionName("非标准流程");
                    taskVo.setName(dcwsNormalTaskVo.getTaskName());
                    taskVo.setBusinessStatus(dcwsNormalTaskVo.getStatus());
                    taskVo.setStartTime(dcwsNormalTaskVo.getCreateTime());
                    taskVo.setName(dcwsNormalTaskVo.getTaskName());
                    SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(dcwsNormalTaskVo.getUserId()));
                    taskVo.setAssignee(Long.valueOf(dcwsNormalTaskVo.getUserId()));
                    taskVo.setAssigneeName(sysUserVo.getUserName());
                    taskVo.setId(String.valueOf(dcwsNormalTaskVo.getTaskId()));
                    taskVo.setBusinessKey(String.valueOf(dcwsNormalTaskVo.getTaskId()));
                    taskVo.setApplyReason(dcwsNormalTaskVo.getRemark());
                    taskVo.setWfType("20");
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
    public TableDataInfo<DcwsTaskVo> getPageByTaskCopy(DcwsTaskBo taskBo, PageQuery pageQuery) {
        return actTaskService.getPageByTaskCopy(taskBo, pageQuery);
    }

    /**
     * 查询当前用户的待办汇总数据
     *
     */
    @GetMapping("/getUserTaskCount")
    public R<DcwsTaskCountVo> getUserTaskCount() {
        return R.ok(actTaskService.getUserTaskCount());
    }
}
