package com.formssi.generator.service;

import com.formssi.generator.domain.GenerateHistory;
import com.formssi.generator.domain.dto.GeneratorCompareDTO;
import com.formssi.generator.param.GeneratorHistoryParam;
import com.formssi.generator.param.GeneratorParam;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/5 16:36
 */
public interface IGenerateHistoryService {

    /**
     * 保存代码生成历史
     * @param param 生成参数
     */
    void saveHistory(GeneratorParam param);

    /**
     * 保存代码生成历史
     * @param historyParam 生成历史参数
     */
    void saveGenerateHistory(GeneratorHistoryParam historyParam);

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    List<GenerateHistory> listAll();


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    GenerateHistory getById(Integer id);

    /**
     * 新增，插入所有字段
     *
     * @param generateHistory 新增的记录
     * @return 返回影响行数
     */
    int insert(GenerateHistory generateHistory);

    /**
     * 新增，忽略null字段
     *
     * @param generateHistory 新增的记录
     * @return 返回影响行数
     */
    int insertIgnoreNull(GenerateHistory generateHistory);

    /**
     * 修改，修改所有字段
     *
     * @param generateHistory 修改的记录
     * @return 返回影响行数
     */
    int update(GenerateHistory generateHistory);

    /**
     * 修改，忽略null字段
     *
     * @param generateHistory 修改的记录
     * @return 返回影响行数
     */
    int updateIgnoreNull(GenerateHistory generateHistory);

    /**
     * 删除记录
     *
     * @param generateHistory 待删除的记录
     * @return 返回影响行数
     */
    int delete(GenerateHistory generateHistory);

    /**
     * 根据版本获取代码生成历史记录
     * @param versionList 版本列表
     * @return 返回代码生成历史记录列表
     */
    List<GenerateHistory> getListByVersionList(List<Integer> versionList);

}
