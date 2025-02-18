package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.generator.domain.DatasourceConfig;

import java.util.List;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DatasourceConfigMapper {

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    List<DatasourceConfig> listAll();


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    DatasourceConfig getById(Integer id);

    /**
     * 根据主键id列表查询
     * @param idList 主键id列表
     * @return 返回数据源列表
     */
    List<DatasourceConfig> getByIdList(List<Integer> idList);

    /**
     * 新增，插入所有字段
     *
     * @param datasourceConfig 新增的记录
     * @return 返回影响行数
     */
    int insert(DatasourceConfig datasourceConfig);

    /**
     * 新增，忽略null字段
     *
     * @param datasourceConfig 新增的记录
     * @return 返回影响行数
     */
    int insertIgnoreNull(DatasourceConfig datasourceConfig);

    /**
     * 修改，修改所有字段
     *
     * @param datasourceConfig 修改的记录
     * @return 返回影响行数
     */
    int update(DatasourceConfig datasourceConfig);

    /**
     * 修改，忽略null字段
     *
     * @param datasourceConfig 修改的记录
     * @return 返回影响行数
     */
    int updateIgnoreNull(DatasourceConfig datasourceConfig);

    /**
     * 删除记录
     *
     * @param datasourceConfig 待删除的记录
     * @return 返回影响行数
     */
    int delete(DatasourceConfig datasourceConfig);

}
