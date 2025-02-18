package com.formssi.generator.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmTable;

import java.util.List;

/**
 * 业务 数据层
 *
 * @author lizhangyu
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmTableMapper extends BaseMapperPlus<DmTable, DmTable> {

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<DmTable> selectGenTableAll();

    /**
     * 查询表ID业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    DmTable selectGenTableById(Long id);

    /**
     * 查询表名称业务信息
     *
     * @param tableName 表名称
     * @return 业务信息
     */
    DmTable selectGenTableByName(String tableName);

    @DS("")
    List<String> selectTableNameList(String dataName);

}
