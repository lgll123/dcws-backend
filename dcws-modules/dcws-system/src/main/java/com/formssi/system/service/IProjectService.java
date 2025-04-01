package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.SysProject;
import com.formssi.system.domain.bo.ProjectBo;
import com.formssi.system.domain.vo.ProjectTreeVo;
import com.formssi.system.domain.vo.ProjectVo;

import java.util.Collection;
import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 12:12
 */
public interface IProjectService extends IService<SysProject> {

    /**
     * 获取spaceId
     *
     * @param projectId 项目id
     * @return 返回空间id
     */
    Long getSpaceId(Long projectId);

    /**
     * 获取所有项目列表
     * @return 返回
     */
    List<SysProject> getAllProject();

    /**
     * 查询项目
     *
     * @param id 主键
     * @return 项目
     */
    ProjectVo queryById(Long id);

    /**
     * 分页查询项目列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目分页列表
     */
    TableDataInfo<ProjectVo> queryPageList(ProjectBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的项目列表
     *
     * @param bo 查询条件
     * @return 项目列表
     */
    List<ProjectVo> queryList(ProjectBo bo);

    /**
     * 新增项目
     *
     * @param bo 项目
     * @return 是否新增成功
     */
    Boolean insertByBo(ProjectBo bo);

    /**
     * 修改项目
     *
     * @param bo 项目
     * @return 是否修改成功
     */
    Boolean updateByBo(ProjectBo bo);

    /**
     * 校验并批量删除项目信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<ProjectTreeVo> tree();

}
