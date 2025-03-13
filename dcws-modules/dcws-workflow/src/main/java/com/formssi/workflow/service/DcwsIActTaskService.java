package com.formssi.workflow.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.domain.vo.DcwsTaskVo;

/**
 * 任务 服务层
 *
 * @author may
 */
public interface DcwsIActTaskService {

    /**
     * 查询当前用户的待办任务
     *
     * @param taskBo    参数
     * @param pageQuery 分页
     * @return 结果
     */
    TableDataInfo<DcwsTaskVo> getPageByTaskWait(DcwsTaskBo taskBo, PageQuery pageQuery);

    /**
     * 查询当前用户的已办任务
     *
     * @param taskBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<DcwsTaskVo> getPageByTaskFinish(DcwsTaskBo taskBo, PageQuery pageQuery);

    /**
     * 查询当前用户的抄送
     *
     * @param taskBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<DcwsTaskVo> getPageByTaskCopy(DcwsTaskBo taskBo, PageQuery pageQuery);

}
