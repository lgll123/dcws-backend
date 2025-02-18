package com.formssi.generator.service.impl;

import com.formssi.generator.domain.TemplateGroup;
import com.formssi.generator.mapper.TemplateConfigMapper;
import com.formssi.generator.mapper.TemplateGroupMapper;
import com.formssi.generator.service.ITemplateGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : zsljava
 * @date Date : 2020-12-15 9:50
 * @Description: TODO
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TemplateGroupServiceImpl implements ITemplateGroupService {

    private final TemplateGroupMapper templateGroupMapper;

    private final TemplateConfigMapper templateConfigMapper;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @Override
    public List<TemplateGroup> listAll() {
        return templateGroupMapper.listAll();
    }


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    @Override
    public TemplateGroup getById(Integer id) {
        return templateGroupMapper.getById(id);
    }

    /**
     * 新增，插入所有字段
     *
     * @param templateGroup 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insert(TemplateGroup templateGroup) {
        return templateGroupMapper.insert(templateGroup);
    }

    /**
     * 新增，忽略null字段
     *
     * @param templateGroup 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insertIgnoreNull(TemplateGroup templateGroup) {
        templateGroup.setIsDeleted(0);
        return templateGroupMapper.insertIgnoreNull(templateGroup);
    }

    /**
     * 修改，修改所有字段
     *
     * @param templateGroup 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int update(TemplateGroup templateGroup) {
        return templateGroupMapper.update(templateGroup);
    }

    /**
     * 修改，忽略null字段
     *
     * @param templateGroup 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int updateIgnoreNull(TemplateGroup templateGroup) {
        return templateGroupMapper.updateIgnoreNull(templateGroup);
    }

    /**
     * 删除记录
     *
     * @param templateGroup 待删除的记录
     * @return 返回影响行数
     */
    @Override
    public int delete(TemplateGroup templateGroup) {
        return templateGroupMapper.delete(templateGroup);
    }

    @Override
    public int deleteGroup(TemplateGroup templateGroup) {
        List<TemplateGroup> templateGroups = this.listAll();
        if (templateGroups.size() == 1) {
            throw new RuntimeException("无法删除，必须要有一个模板组");
        }
        int delete = templateGroupMapper.delete(templateGroup);
        templateConfigMapper.deleteByGroupId(templateGroup.getId());
        return delete;
    }

    @Override
    public TemplateGroup getByName(String name) {
        return templateGroupMapper.getByName(name);
    }
}
