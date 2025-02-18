package com.formssi.generator.service;

import com.formssi.generator.domain.TemplateConfig;
import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/5 16:36
 */
public interface ITemplateConfigService {

    /**
     * 获取模版列表
     * @param idList 模版id列表
     * @return 返回
     */
    List<TemplateConfig> listTemplate(List<Integer> idList);

    /**
     * 根据主键id获取
     * @param id 主键id
     * @return 返回模版
     */
    TemplateConfig getById(int id);

    /**
     * 获取所有模版
     * @return 返回所有模版
     */
    List<TemplateConfig> listAll();

    /**
     * 插入模版
     * @param templateConfig 模版
     */
    void insert(TemplateConfig templateConfig);

    /**
     * 更新模版
     * @param templateConfig 模版
     */
    void update(TemplateConfig templateConfig);

    /**
     * 删除模版
     * @param templateConfig 模版
     */
    void delete(TemplateConfig templateConfig);

    /**
     * 根据模版组id获取模版
     * @param groupId 模版组id
     * @return 模版模版列表
     */
    List<TemplateConfig> listByGroupId(String groupId);

    /**
     * 保存模版
     * @param templateConfig 模版
     */
    void save(TemplateConfig templateConfig);

    /**
     * 复制模板
     * @param templateConfig
     */
    void copy(TemplateConfig templateConfig);

}
