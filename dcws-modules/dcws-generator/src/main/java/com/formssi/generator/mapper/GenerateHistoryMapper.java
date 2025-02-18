package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.generator.domain.GenerateHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface GenerateHistoryMapper {

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
	 * 根据md5查询
	 *
	 * @param md5 md5
	 * @return 返回记录，没有返回null
	 */
	GenerateHistory getByMd5(@Param("md5") String md5);

	/**
	 * 获取最大版本
	 * @return 返回版本
	 */
	int queryMaxVersion();

	/**
	 * 根据版本列表获取代码生成历史
	 * @param versionList 版本列表
	 * @return 返回代码生成历史
	 */
	List<GenerateHistory> getListByVersionList(@Param("versionList") List<Integer> versionList);
}