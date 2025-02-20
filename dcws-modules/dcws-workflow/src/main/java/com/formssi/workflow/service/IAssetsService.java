package com.formssi.workflow.service;



import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.AssetsBo;
import com.formssi.workflow.domain.vo.AssetsVo;

import java.util.Collection;
import java.util.List;

/**
 * 请假Service接口
 *
 * @author may
 * @date 2023-07-21
 */
public interface IAssetsService {

    /**
     * 查询请假
     */
    AssetsVo queryById(Long id);

    /**
     * 查询请假列表
     */
    TableDataInfo<AssetsVo> queryPageList(AssetsBo bo, PageQuery pageQuery);

    /**
     * 查询请假列表
     */
    List<AssetsVo> queryList(AssetsBo bo);

    /**
     * 新增请假
     */
    AssetsVo insertByBo(AssetsBo bo);

    /**
     * 修改请假
     */
    AssetsVo updateByBo(AssetsBo bo);

    /**
     * 校验并批量删除请假信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);
}
