package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.generator.domain.TypeConfig;

import java.util.List;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface TypeConfigMapper {

	/**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
	List<TypeConfig> listAll();


	/**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
	TypeConfig getById(Integer id);

	/**
     * 新增，插入所有字段
     *
     * @param typeConfig 新增的记录
     * @return 返回影响行数
     */
	int insert(TypeConfig typeConfig);

	/**
     * 新增，忽略null字段
     *
     * @param typeConfig 新增的记录
     * @return 返回影响行数
     */
	int insertIgnoreNull(TypeConfig typeConfig);

	/**
     * 修改，修改所有字段
     *
     * @param typeConfig 修改的记录
     * @return 返回影响行数
     */
	int update(TypeConfig typeConfig);

	/**
     * 修改，忽略null字段
     *
     * @param typeConfig 修改的记录
     * @return 返回影响行数
     */
	int updateIgnoreNull(TypeConfig typeConfig);

	/**
     * 删除记录
     *
     * @param typeConfig 待删除的记录
     * @return 返回影响行数
     */
	int delete(TypeConfig typeConfig);

}
