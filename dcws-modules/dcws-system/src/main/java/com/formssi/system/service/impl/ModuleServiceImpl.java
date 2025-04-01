package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.SysModule;
import com.formssi.system.domain.bo.ModuleBo;
import com.formssi.system.domain.vo.ModuleVo;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.mapper.SysModuleMapper;
import com.formssi.system.service.IModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 14:00
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ModuleServiceImpl extends ServiceImpl<SysModuleMapper, SysModule> implements IModuleService {

    private final SysModuleMapper baseMapper;

    @Override
    public List<SysModule> listProjectModules(long projectId) {
        return lambdaQuery()
                .eq(SysModule::getProjectId, projectId)
                .eq(SysModule::getIsDeleted, BooleanEnum.FALSE.getType())
                .orderByAsc(SysModule::getOrderIndex)
                .list();
    }

    /**
     * 查询项目模块
     *
     * @param id 主键
     * @return 项目模块
     */
    @Override
    public ModuleVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询项目模块列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目模块分页列表
     */
    @Override
    public TableDataInfo<ModuleVo> queryPageList(ModuleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysModule> lqw = buildQueryWrapper(bo);
        Page<ModuleVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的项目模块列表
     *
     * @param bo 查询条件
     * @return 项目模块列表
     */
    @Override
    public List<ModuleVo> queryList(ModuleBo bo) {
        LambdaQueryWrapper<SysModule> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysModule> buildQueryWrapper(ModuleBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysModule> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SysModule::getName, bo.getName());
        lqw.eq(bo.getProjectId() != null, SysModule::getProjectId, bo.getProjectId());
        lqw.eq(bo.getType() != null, SysModule::getType, bo.getType());
        lqw.eq(StringUtils.isNotBlank(bo.getImportUrl()), SysModule::getImportUrl, bo.getImportUrl());
        lqw.like(StringUtils.isNotBlank(bo.getBasicAuthUsername()), SysModule::getBasicAuthUsername, bo.getBasicAuthUsername());
        lqw.eq(StringUtils.isNotBlank(bo.getBasicAuthPassword()), SysModule::getBasicAuthPassword, bo.getBasicAuthPassword());
        lqw.eq(StringUtils.isNotBlank(bo.getToken()), SysModule::getToken, bo.getToken());
        lqw.eq(bo.getCreateMode() != null, SysModule::getCreateMode, bo.getCreateMode());
        lqw.eq(bo.getModifyMode() != null, SysModule::getModifyMode, bo.getModifyMode());
        lqw.eq(bo.getCreatorId() != null, SysModule::getCreatorId, bo.getCreatorId());
        lqw.eq(bo.getModifierId() != null, SysModule::getModifierId, bo.getModifierId());
        lqw.eq(bo.getOrderIndex() != null, SysModule::getOrderIndex, bo.getOrderIndex());
        lqw.eq(bo.getIsDeleted() != null, SysModule::getIsDeleted, bo.getIsDeleted());
        lqw.eq(bo.getGmtCreate() != null, SysModule::getGmtCreate, bo.getGmtCreate());
        lqw.eq(bo.getGmtModified() != null, SysModule::getGmtModified, bo.getGmtModified());
        return lqw;
    }

    /**
     * 新增项目模块
     *
     * @param bo 项目模块
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ModuleBo bo) {
        SysModule add = MapstructUtils.convert(bo, SysModule.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改项目模块
     *
     * @param bo 项目模块
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ModuleBo bo) {
        SysModule update = MapstructUtils.convert(bo, SysModule.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysModule entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除项目模块信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

}
