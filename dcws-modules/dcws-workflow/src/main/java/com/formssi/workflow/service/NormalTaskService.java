package com.formssi.workflow.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.DcwsNormalTaskBo;
import com.formssi.workflow.domain.vo.DcwsNormalTaskHandleHisVo;
import com.formssi.workflow.domain.vo.DcwsNormalTaskVo;
import com.formssi.workflow.domain.vo.DcwsTaskTypeVo;

import java.util.List;

public interface NormalTaskService {

    /**
     * 查询非标准流程
     */
    DcwsNormalTaskVo queryById(Long id);

    /**
     * 查询非标准流程列表
     */
    TableDataInfo<DcwsNormalTaskVo> queryPageList(DcwsNormalTaskBo bo, PageQuery pageQuery);

    /**
     * 查询当前用户的待办任务
     *
     * @param dcwsNormalTaskBo    参数
     * @param pageQuery 分页
     * @return 结果
     */
    TableDataInfo<DcwsNormalTaskVo> getPageByTaskWait(DcwsNormalTaskBo dcwsNormalTaskBo, PageQuery pageQuery);


    /**
     * 查询当前用户的已办任务
     *
     * @param dcwsNormalTaskBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<DcwsNormalTaskVo> getPageByTaskFinish(DcwsNormalTaskBo dcwsNormalTaskBo, PageQuery pageQuery);

    /**
     * 查询非标准流程列表
     */
    List<DcwsNormalTaskVo> queryList(DcwsNormalTaskBo bo);

    /**
     * 新增非标准流程
     */
    DcwsNormalTaskVo insertByBo(DcwsNormalTaskBo bo);

    /**
     * 修改非标准流程
     */
    DcwsNormalTaskVo updateByBo(DcwsNormalTaskBo bo);

    /**
     * 撤销流程申请
     *
     * @param id 流程id
     * @return 结果
     */
    boolean cancelProcessApply(String id);

    /**
     * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     *
     * @param id 业务id
     * @return 结果
     */
    boolean deleteRunAndHisInstance(String id);

    /**
     * 获取审批记录
     *
     * @param id 流程id
     * @return 结果
     */
    List<DcwsNormalTaskHandleHisVo> getHistoryRecord(Long id);

    /**
     * 查询流程类型
     *
     */
    List<DcwsTaskTypeVo> queryWfType();
}
