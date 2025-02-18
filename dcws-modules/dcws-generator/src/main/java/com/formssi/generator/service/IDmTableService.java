package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.domain.DmTable;
import com.formssi.generator.domain.DmTableColumn;
import com.formssi.generator.domain.dto.GeneratorCompareDTO;
import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.sql.CodeFile;
import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.TableDefinition;

import java.util.List;
import java.util.Map;

/**
 * 业务 服务层
 *
 * @author Lion Li
 */
public interface IDmTableService extends IService<DmTable> {

    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    List<DmTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * 查询业务列表
     *
     * @param dmTable 业务信息
     * @return 业务集合
     */
    TableDataInfo<DmTable> selectPageGenTableList(DmTable dmTable, PageQuery pageQuery);

    /**
     * 查询据库列表
     *
     * @param dmTable 业务信息
     * @return 数据库表集合
     */
    TableDataInfo<DmTable> selectPageDbTableList(DmTable dmTable, PageQuery pageQuery);

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @param dataName   数据源名称
     * @return 数据库表集合
     */
    List<DmTable> selectDbTableListByNames(String[] tableNames, String dataName);

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<DmTable> selectGenTableAll();

    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    DmTable selectGenTableById(Long id);

    /**
     * 修改业务
     *
     * @param dmTable 业务信息
     */
    void updateGenTable(DmTable dmTable);

    /**
     * 删除业务信息
     *
     * @param tableIds 需要删除的表数据ID
     */
    void deleteGenTableByIds(Long[] tableIds);

    /**
     * 导入表结构
     *
     * @param tableList 导入表列表
     * @param dataName  数据源名称
     */
    void importGenTable(List<DmTable> tableList, String dataName);

    /**
     * 导入表列表结构
     *
     * @param tableList 导入表列表
     * @param dataName  数据源名称
     * @param dataSourceId  数据源id
     */
    void importGenTableList(List<TableDefinition> tableList, String dataName, Integer dataSourceId);

    /**
     * 根据表名称查询列信息
     *
     * @param tableName 表名称
     * @param dataName  数据源名称
     * @return 列信息
     */
    List<DmTableColumn> selectDbTableColumnsByName(String tableName, String dataName);

    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表名称
     * @return 数据
     */
    byte[] downloadCode(Long tableId);

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表名称
     */
    void generatorCode(Long tableId);

    /**
     * 同步数据库
     *
     * @param tableId 表名称
     */
    void synchDb(Long tableId);

    /**
     * 批量生成代码（下载方式）
     *
     * @param tableIds 表ID数组
     * @return 数据
     */
    byte[] downloadCode(String[] tableIds);

    /**
     * 批量代码生成（下载方式）
     *
     * @param tableIdList 表ID列表
     * @param templateConfigIdList 模版ID列表
     * @return 数据
     */
    byte[] batchCodeGenerate(List<Long> tableIdList, List<Integer> templateConfigIdList);

    /**
     * 修改保存参数校验
     *
     * @param dmTable 业务信息
     */
    void validateEdit(DmTable dmTable);

    /**
     * 根据数据源id和表名获取表结构
     * @param datasourceConfigId 数据源id
     * @param tableNames 表名列表
     * @return 返回表结构列表
     */
    List<TableDefinition> getListByDatasourceConfigIdAndTableNames(Integer datasourceConfigId, List<String> tableNames);

    /**
     * 根据表id列表和
     * @param tableIdList 表id列表
     * @param templateConfigIdList 模版id列表
     * @return 返回文件列表
     */
    List<CodeFile> getCodeListByTableIdsAndTemplateConfigIds(List<Long> tableIdList, List<Integer> templateConfigIdList);

    /**
     * 不同版本代码比较
     * @param currentVersion 当前版本
     * @param compareVersion 比较版本
     * @return 返回
     */
    GeneratorCompareDTO compareByVersion(Integer currentVersion, Integer compareVersion);

    /**
     * 生成代码
     * @param tableId
     * @param templateConfigIdList
     * @param datasourceConfigIdList
     * @param tableNameList
     * @return
     */
    List<CodeFile> codeGenerate(Long tableId, List<Integer> templateConfigIdList, List<Integer> datasourceConfigIdList, List<String> tableNameList);

    /**
     * 查询据库列表
     *
     * @param tableName 表名称组
     * @param datasourceConfigId   数据源id
     * @return 数据库表集合
     */
    DmTable selectDbTableByNamesAndDatasourceConfigId(String tableName, Long datasourceConfigId);

    /**
     * 代码预览
     * @param generatorParam 生成参数
     * @param generatorConfig 生成模版
     * @param datasourceConfigId 数据源id
     * @return 返回代码生成map
     */
    Map<String, String> codePreview(GeneratorParam generatorParam, GeneratorConfig generatorConfig, int datasourceConfigId);

}
