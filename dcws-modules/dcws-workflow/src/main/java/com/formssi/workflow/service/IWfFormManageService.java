package com.formssi.workflow.service;

import com.formssi.workflow.domain.vo.WfFormManageVo;
import com.formssi.workflow.domain.bo.WfFormManageBo;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 表单管理Service接口
 *
 * @author may
 * @date 2024-03-29
 */
public interface IWfFormManageService {

    /**
     * 查询表单管理
     *
     * @param id 主键
     * @return 结果
     */
    WfFormManageVo queryById(Long id);

    /**
     * 查询表单管理
     *
     * @param ids 主键
     * @return 结果
     */
    List<WfFormManageVo> queryByIds(List<Long> ids);

    /**
     * 查询表单管理列表
     *
     * @param bo        参数
     * @param pageQuery 分页
     * @return 结果
     */
    TableDataInfo<WfFormManageVo> queryPageList(WfFormManageBo bo, PageQuery pageQuery);

    /**
     * 查询表单管理列表
     *
     * @return 结果
     */
    List<WfFormManageVo> selectList();
    /**
     * 查询表单管理列表
     *
     * @param bo 参数
     * @return 结果
     */
    List<WfFormManageVo> queryList(WfFormManageBo bo);

    /**
     * 新增表单管理
     *
     * @param bo 参数
     * @return 结果
     */
    Boolean insertByBo(WfFormManageBo bo);

    /**
     * 修改表单管理
     *
     * @param bo 参数
     * @return 结果
     */
    Boolean updateByBo(WfFormManageBo bo);

    /**
     * 批量删除表单管理信息
     *
     * @param ids 主键
     * @return 结果
     */
    Boolean deleteByIds(Collection<Long> ids);

    /**
     * 通过前端路由找表单Key
     * @param router 前端路由
     * @return 表单Key
     */
    WfFormManageVo queryByRouter(String router);

}
