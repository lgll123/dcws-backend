package com.formssi.generator.service.impl;

import com.formssi.generator.converter.ColumnTypeConverter;
import com.formssi.generator.converter.DbColumnTypeConverter;
import com.formssi.generator.domain.TypeConfig;
import com.formssi.generator.mapper.TypeConfigMapper;
import com.formssi.generator.service.ITypeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TypeConfigServiceImpl implements ITypeConfigService {

    private final TypeConfigMapper typeConfigMapper;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @Override
    public List<TypeConfig> listAll() {
    	return typeConfigMapper.listAll();
    }


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    @Override
    public TypeConfig getById(Integer id) {
    	return typeConfigMapper.getById(id);
    }

    /**
     * 新增，插入所有字段
     *
     * @param typeConfig 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insert(TypeConfig typeConfig) {
    	return typeConfigMapper.insert(typeConfig);
    }

    /**
     * 新增，忽略null字段
     *
     * @param typeConfig 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insertIgnoreNull(TypeConfig typeConfig) {
    	return typeConfigMapper.insertIgnoreNull(typeConfig);
    }

    /**
     * 修改，修改所有字段
     *
     * @param typeConfig 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int update(TypeConfig typeConfig) {
    	return typeConfigMapper.update(typeConfig);
    }

    /**
     * 修改，忽略null字段
     *
     * @param typeConfig 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int updateIgnoreNull(TypeConfig typeConfig) {
    	return typeConfigMapper.updateIgnoreNull(typeConfig);
    }

    /**
     * 删除记录
     *
     * @param typeConfig 待删除的记录
     * @return 返回影响行数
     */
    @Override
    public int delete(TypeConfig typeConfig) {
    	return typeConfigMapper.delete(typeConfig);
    }

    /**
     * 构建列和类型之间的转换
     * @return 返回数据库类型转换成各语言对应的类型
     */
    @Override
    public ColumnTypeConverter buildColumnTypeConverter() {
        List<TypeConfig> typeConfigs = typeConfigMapper.listAll();
        Map<String, TypeConfig> map = typeConfigs.stream()
                .collect(Collectors.toMap(TypeConfig::getDbType, Function.identity(), (v1, v2) -> v2));
        return new DbColumnTypeConverter(map);
    }
}
