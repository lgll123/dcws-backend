package com.formssi.workflow.service;



import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.bo.AssetsBo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.domain.vo.AssetsVo;

import java.util.Collection;
import java.util.List;

/**
 * 申请Service接口
 *
 * @author may
 * @date 2023-07-21
 */
public interface IApplyService {

    /**
     * 查询申请
     */
    TaskNodeDataVo queryById(Long id);

    /**
     * 查询申请列表
     */
    TableDataInfo<TaskNodeDataVo> queryPageList(TaskNodeDataBo bo, PageQuery pageQuery);

    /**
     * 查询申请列表
     */
    List<TaskNodeDataVo> queryList(TaskNodeDataBo bo);

    /**
     * 新增申请
     */
    TaskNodeDataVo insertByBo(TaskNodeDataBo bo);

    /**
     * 修改申请
     */
    AssetsVo updateByBo(AssetsBo bo);

    /**
     * 校验并批量删除申请信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);
}
