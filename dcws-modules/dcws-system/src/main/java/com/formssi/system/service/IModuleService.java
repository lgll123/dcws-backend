package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.SysModule;
import com.formssi.system.domain.bo.ModuleBo;
import com.formssi.system.domain.vo.ModuleVo;

import java.util.Collection;
import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 11:49
 */
public interface IModuleService extends IService<SysModule> {

    /**
     * 根据项目id获取所有模块列表
     * @param projectId 项目id
     * @return 返回模块列表
     */
    List<SysModule> listProjectModules(long projectId);

    /**
     * 查询项目模块
     *
     * @param id 主键
     * @return 项目模块
     */
    ModuleVo queryById(Long id);

    /**
     * 分页查询项目模块列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目模块分页列表
     */
    TableDataInfo<ModuleVo> queryPageList(ModuleBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的项目模块列表
     *
     * @param bo 查询条件
     * @return 项目模块列表
     */
    List<ModuleVo> queryList(ModuleBo bo);

    /**
     * 新增项目模块
     *
     * @param bo 项目模块
     * @return 是否新增成功
     */
    Boolean insertByBo(ModuleBo bo);

    /**
     * 修改项目模块
     *
     * @param bo 项目模块
     * @return 是否修改成功
     */
    Boolean updateByBo(ModuleBo bo);

    /**
     * 校验并批量删除项目模块信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
