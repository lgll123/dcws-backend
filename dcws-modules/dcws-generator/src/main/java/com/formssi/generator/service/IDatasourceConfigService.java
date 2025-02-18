package com.formssi.generator.service;

import com.formssi.generator.domain.DatasourceConfig;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/5 16:37
 */
public interface IDatasourceConfigService {

    /**
     * 根据主键id获取数据源配置
     * @param id 主键id
     * @return 返回数据源配置
     */
    DatasourceConfig getById(int id);

    /**
     * 根据数据源id列表获取数据源配置列表
     * @param idList 数据源id列表
     * @return 返回数据源配置列表
     */
    List<DatasourceConfig> getByIdList(List<Integer> idList);

    /**
     * 获取所有数据源数据
     * @return 返回所有数据源
     */
    List<DatasourceConfig> listAll();

    /**
     * 插入数据源
     * @param datasourceConfig 数据源配置
     */
    void insert(DatasourceConfig datasourceConfig);

    /**
     * 更新数据源
     * @param datasourceConfig 数据源配置
     */
    void update(DatasourceConfig datasourceConfig);

    /**
     * 删除数据源
     * @param datasourceConfig 数据源配置
     */
    void delete(DatasourceConfig datasourceConfig);
}
