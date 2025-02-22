package com.formssi.workflow.service;

import com.formssi.workflow.domain.bo.DcwsProjectBo;
import com.formssi.workflow.domain.bo.DcwsProjectTaskBo;
import com.formssi.workflow.domain.vo.DcwsProjectTaskVo;
import com.formssi.workflow.domain.vo.DcwsProjectVo;

import java.util.List;

public interface ProjectManagementService {

    /**
     * 查询项目详情
     */
    DcwsProjectVo queryById(Long id);

    /**
     * 查询项目列表
     */
    List<DcwsProjectVo> queryProjectList(DcwsProjectBo bo);

    /**
     * 查询项目下任务列表
     */
    List<DcwsProjectTaskVo> queryProjectTaskList(DcwsProjectTaskBo bo);

    /**
     * 新增项目
     */
    DcwsProjectVo insertByBo(DcwsProjectBo bo);

    /**
     * 修改项目
     */
    DcwsProjectVo updateByBo(DcwsProjectBo bo);

    /**
     * 修改任务
     */
    DcwsProjectTaskVo updateByTaskBo(DcwsProjectTaskBo bo);

}
