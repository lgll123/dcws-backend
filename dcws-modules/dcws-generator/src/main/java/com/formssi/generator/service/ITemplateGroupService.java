package com.formssi.generator.service;

import com.formssi.generator.domain.TemplateGroup;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/5 16:37
 */
public interface ITemplateGroupService {

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    List<TemplateGroup> listAll();


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    TemplateGroup getById(Integer id);

    /**
     * 新增，插入所有字段
     *
     * @param templateGroup 新增的记录
     * @return 返回影响行数
     */
    int insert(TemplateGroup templateGroup);

    /**
     * 新增，忽略null字段
     *
     * @param templateGroup 新增的记录
     * @return 返回影响行数
     */
    int insertIgnoreNull(TemplateGroup templateGroup);

    /**
     * 修改，修改所有字段
     *
     * @param templateGroup 修改的记录
     * @return 返回影响行数
     */
    int update(TemplateGroup templateGroup);

    /**
     * 修改，忽略null字段
     *
     * @param templateGroup 修改的记录
     * @return 返回影响行数
     */
    int updateIgnoreNull(TemplateGroup templateGroup);

    /**
     * 删除记录
     *
     * @param templateGroup 待删除的记录
     * @return 返回影响行数
     */
    int delete(TemplateGroup templateGroup);

    int deleteGroup(TemplateGroup templateGroup);

    TemplateGroup getByName(String name);
}
