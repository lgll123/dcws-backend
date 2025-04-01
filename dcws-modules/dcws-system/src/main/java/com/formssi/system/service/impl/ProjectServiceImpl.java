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
import com.formssi.system.domain.SysProject;
import com.formssi.system.domain.bo.ProjectBo;
import com.formssi.system.domain.vo.ProjectTreeVo;
import com.formssi.system.domain.vo.ProjectVo;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.mapper.SysModuleMapper;
import com.formssi.system.mapper.SysProjectMapper;
import com.formssi.system.service.IProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lizhangyu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectServiceImpl extends ServiceImpl<SysProjectMapper, SysProject> implements InitializingBean, IProjectService {

    // key: projectId, value:spaceId
    private static final Map<Long, Long> projectIdSpaceIdMap = new HashMap<>(8);

    private final SysProjectMapper baseMapper;

    private final SysModuleMapper moduleMapper;

    @Override
    public Long getSpaceId(Long projectId) {
        return projectIdSpaceIdMap.computeIfAbsent(projectId, k -> {
            SysProject project = getById(projectId);
            return Optional.ofNullable(project).map(SysProject::getSpaceId).orElse(null);
        });
    }

    @Override
    public List<SysProject> getAllProject() {
        return lambdaQuery()
                .eq(SysProject::getIsDeleted, BooleanEnum.FALSE.getType())
                .list();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<Long, Long> map = getAllProject()
                .stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getSpaceId));
        projectIdSpaceIdMap.putAll(map);
    }

    /**
     * 查询项目
     *
     * @param id 主键
     * @return 项目
     */
    @Override
    public ProjectVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询项目列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目分页列表
     */
    @Override
    public TableDataInfo<ProjectVo> queryPageList(ProjectBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysProject> lqw = buildQueryWrapper(bo);
        Page<ProjectVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的项目列表
     *
     * @param bo 查询条件
     * @return 项目列表
     */
    @Override
    public List<ProjectVo> queryList(ProjectBo bo) {
        LambdaQueryWrapper<SysProject> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysProject> buildQueryWrapper(ProjectBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysProject> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SysProject::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getDescription()), SysProject::getDescription, bo.getDescription());
        lqw.eq(bo.getSpaceId() != null, SysProject::getSpaceId, bo.getSpaceId());
        lqw.eq(bo.getIsPrivate() != null, SysProject::getIsPrivate, bo.getIsPrivate());
        lqw.eq(bo.getCreatorId() != null, SysProject::getCreatorId, bo.getCreatorId());
        lqw.like(StringUtils.isNotBlank(bo.getCreatorName()), SysProject::getCreatorName, bo.getCreatorName());
        lqw.eq(bo.getModifierId() != null, SysProject::getModifierId, bo.getModifierId());
        lqw.like(StringUtils.isNotBlank(bo.getModifierName()), SysProject::getModifierName, bo.getModifierName());
        lqw.eq(bo.getOrderIndex() != null, SysProject::getOrderIndex, bo.getOrderIndex());
        lqw.eq(bo.getIsDeleted() != null, SysProject::getIsDeleted, bo.getIsDeleted());
        lqw.eq(bo.getGmtCreate() != null, SysProject::getGmtCreate, bo.getGmtCreate());
        lqw.eq(bo.getGmtModified() != null, SysProject::getGmtModified, bo.getGmtModified());
        return lqw;
    }

    /**
     * 新增项目
     *
     * @param bo 项目
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ProjectBo bo) {
        SysProject add = MapstructUtils.convert(bo, SysProject.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改项目
     *
     * @param bo 项目
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ProjectBo bo) {
        SysProject update = MapstructUtils.convert(bo, SysProject.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysProject entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除项目信息
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

    public List<ProjectTreeVo> tree() {
        List<ProjectTreeVo> result = new ArrayList();
        List<SysProject> sysProjectList = this.baseMapper.selectList();
        if (CollectionUtils.isEmpty(sysProjectList)) {
            return result;
        } else {
            List<SysModule> sysModuleList = this.moduleMapper.selectList();
            Map<Long, List<ProjectTreeVo>> projectModulesMap = new HashMap();
            Iterator var5;
            ProjectTreeVo projectTreeVo;
            if (CollectionUtils.isNotEmpty(sysModuleList)) {
                var5 = sysModuleList.iterator();

                while(var5.hasNext()) {
                    SysModule sysModule = (SysModule)var5.next();
                    if (!projectModulesMap.containsKey(sysModule.getProjectId())) {
                        projectModulesMap.put(sysModule.getProjectId(), new ArrayList());
                    }

                    projectTreeVo = new ProjectTreeVo();
                    projectTreeVo.setId(sysModule.getId());
                    projectTreeVo.setName(sysModule.getName());
                    projectTreeVo.setType(2);
                    ((List)projectModulesMap.get(sysModule.getProjectId())).add(projectTreeVo);
                }
            }

            var5 = sysProjectList.iterator();

            while(var5.hasNext()) {
                SysProject sysProject = (SysProject)var5.next();
                projectTreeVo = new ProjectTreeVo();
                projectTreeVo.setId(sysProject.getId());
                projectTreeVo.setName(sysProject.getName());
                projectTreeVo.setType(1);
                projectTreeVo.setChildren((List)projectModulesMap.get(sysProject.getId()));
                result.add(projectTreeVo);
            }

            return result;
        }
    }

}
