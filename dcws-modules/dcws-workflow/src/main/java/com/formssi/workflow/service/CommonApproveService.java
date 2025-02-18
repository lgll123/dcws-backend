package com.formssi.workflow.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.DcwsApproveBo;
import com.formssi.workflow.domain.bo.TaskBo;
import com.formssi.workflow.domain.vo.DcwsApproveVo;
import com.formssi.workflow.domain.vo.TaskVo;

import java.util.Collection;
import java.util.List;

public interface CommonApproveService {

    /**
     * 查询非标准流程
     */
    DcwsApproveVo queryById(Long id);

    /**
     * 查询非标准流程列表
     */
    TableDataInfo<DcwsApproveVo> queryPageList(DcwsApproveBo bo, PageQuery pageQuery);

    /**
     * 查询当前用户的待办任务
     *
     * @param dcwsApproveBo    参数
     * @param pageQuery 分页
     * @return 结果
     */
    TableDataInfo<DcwsApproveVo> getPageByTaskWait(DcwsApproveBo dcwsApproveBo, PageQuery pageQuery);


    /**
     * 查询当前用户的已办任务
     *
     * @param dcwsApproveBo    参数
     * @param pageQuery 参数
     * @return 结果
     */
    TableDataInfo<DcwsApproveVo> getPageByTaskFinish(DcwsApproveBo dcwsApproveBo, PageQuery pageQuery);

    /**
     * 查询非标准流程列表
     */
    List<DcwsApproveVo> queryList(DcwsApproveBo bo);

    /**
     * 新增非标准流程
     */
    DcwsApproveVo insertByBo(DcwsApproveBo bo);

    /**
     * 修改非标准流程
     */
    DcwsApproveVo updateByBo(DcwsApproveBo bo);

    /**
     * 校验并批量删除非标准流程
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);
}
