package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.workflow.domain.WfCategory;
import com.formssi.workflow.domain.bo.WfCategoryBo;
import com.formssi.workflow.domain.vo.WfCategoryVo;
import com.formssi.workflow.mapper.WfCategoryMapper;
import com.formssi.workflow.service.IWfCategoryService;
import com.formssi.workflow.utils.QueryUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 流程分类Service业务层处理
 *
 * @author may
 * @date 2023-06-28
 */
@RequiredArgsConstructor
@Service
public class WfCategoryServiceImpl implements IWfCategoryService {

    private final WfCategoryMapper baseMapper;
    @Autowired(required = false)
    private RepositoryService repositoryService;

    /**
     * 查询流程分类
     */
    @Override
    public WfCategoryVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }


    /**
     * 查询流程分类列表
     */
    @Override
    public List<WfCategoryVo> queryList(WfCategoryBo bo) {
        LambdaQueryWrapper<WfCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<WfCategory> buildQueryWrapper(WfCategoryBo bo) {
        LambdaQueryWrapper<WfCategory> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getCategoryName()), WfCategory::getCategoryName, bo.getCategoryName());
        lqw.eq(StringUtils.isNotBlank(bo.getCategoryCode()), WfCategory::getCategoryCode, bo.getCategoryCode());
        return lqw;
    }

    /**
     * 新增流程分类
     */
    @Override
    public Boolean insertByBo(WfCategoryBo bo) {
        WfCategory add = MapstructUtils.convert(bo, WfCategory.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(WfCategoryBo bo) {
        WfCategory update = MapstructUtils.convert(bo, WfCategory.class);
        validEntityBeforeSave(update);
        WfCategoryVo wfCategoryVo = baseMapper.selectVoById(bo.getId());
        List<ProcessDefinition> processDefinitionList = QueryUtils.definitionQuery().processDefinitionCategory(wfCategoryVo.getCategoryCode()).list();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            repositoryService.setProcessDefinitionCategory(processDefinition.getId(), bo.getCategoryCode());
        }
        List<Deployment> deploymentList = QueryUtils.deploymentQuery().deploymentCategory(wfCategoryVo.getCategoryCode()).list();
        for (Deployment deployment : deploymentList) {
            repositoryService.setDeploymentCategory(deployment.getId(), bo.getCategoryCode());
        }
        List<Model> modelList = QueryUtils.modelQuery().modelCategory(wfCategoryVo.getCategoryCode()).list();
        for (Model model : modelList) {
            model.setCategory(bo.getCategoryCode());
            repositoryService.saveModel(model);
        }
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(WfCategory entity) {
    }

    /**
     * 批量删除流程分类
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 按照类别编码查询
     *
     * @param categoryCode 分类比吗
     */
    @Override
    public WfCategory queryByCategoryCode(String categoryCode) {
        return baseMapper.selectOne(new LambdaQueryWrapper<WfCategory>().eq(WfCategory::getCategoryCode, categoryCode));
    }
}
