package com.formssi.generator.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.domain.*;
import com.formssi.generator.mapper.*;
import com.formssi.generator.model.request.*;
import com.formssi.generator.service.IDmLayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ER图设计 服务层实现
 *
 * @author Shen Tao
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmLayoutServiceImpl extends ServiceImpl<DmLayoutMapper, DmLayout> implements IDmLayoutService {

    private final DmLayoutMapper dmLayoutMapper;
    private final DmTableMapper dmTableMapper;
    private final DmTableLayoutMapper dmTableLayoutMapper;
    private final DmTableColumnMapper dmTableColumnMapper;
    private final DmTableIndexMapper dmTableIndexMapper;
    private final DmTableIndexColumnMapper dmTableIndexColumnMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public TableDataInfo<DmLayout> findLayoutPage(DmLayoutPageRequest request, PageQuery pageQuery) {
        LambdaQueryWrapper<DmLayout> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DmLayout::getLayoutId, DmLayout::getTitle, DmLayout::getSystemCode, DmLayout::getModuleCode);
        wrapper.like(StringUtils.isNotEmpty(request.getTitle()), DmLayout::getTitle, request.getTitle());
        wrapper.eq(StringUtils.isNotEmpty(request.getSystemCode()), DmLayout::getSystemCode, request.getSystemCode());
        wrapper.eq(StringUtils.isNotEmpty(request.getModuleCode()), DmLayout::getModuleCode, request.getModuleCode());
        Page<DmLayout> page = dmLayoutMapper.selectPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public DmLayout getLayout(Long layoutId) {
        return dmLayoutMapper.selectById(layoutId);
    }

    @Override
    public void addLayout(String requestBody) {
        DmLayoutRequest layoutRequest = JSON.parseObject(requestBody, DmLayoutRequest.class);
        checkLayout(layoutRequest);
        DmLayout layout = new DmLayout();
        layout.setSystemCode(layoutRequest.getSystemCode());
        layout.setModuleCode(layoutRequest.getModuleCode());
        layout.setTitle(layoutRequest.getTitle());
        layout.setLayoutData(requestBody);
        dmLayoutMapper.insert(layout);
    }

    @Override
    public void updateLayout(Long layoutId, String requestBody) {
        DmLayoutRequest layoutRequest = JSON.parseObject(requestBody, DmLayoutRequest.class);
        checkLayout(layoutRequest);
        DmLayout layout = new DmLayout();
        layout.setSystemCode(layoutRequest.getSystemCode());
        layout.setModuleCode(layoutRequest.getModuleCode());
        layout.setTitle(layoutRequest.getTitle());
        layout.setLayoutId(layoutId);
        layout.setLayoutData(requestBody);
        dmLayoutMapper.updateById(layout);
    }

    @Override
    public void deleteLayoutByIds(Long[] layoutIds) {
        dmLayoutMapper.deleteByIds(Arrays.asList(layoutIds));
    }

//    @Override
//    public DmTableVO getTableLayout(Long tableId) {
//        GenTable table = dmTableMapper.selectById(tableId);
//        DmTableVO dmTableVO = new DmTableVO();
//        dmTableVO.setTableId(table.getTableId());
//        dmTableVO.setTableName(table.getTableName());
//        dmTableVO.setTableComment(table.getTableComment());
//        return dmTableVO;
//    }

    @Override
    public void applyTableLayout(DmLayoutTableRequest layoutTableRequest, boolean isForced) {
        DmLayoutTableRequest.TableData tableData = layoutTableRequest.getData();
        List<DmLayoutTableRequest.PortItem> tableItems = layoutTableRequest.getPorts().getItems();
        DmTableRequest dmTableRequest = new DmTableRequest();
        dmTableRequest.setTableId(tableData.getTableId());
        dmTableRequest.setTableName(tableData.getTableName());
        dmTableRequest.setTableComment(tableData.getTableComment());
        dmTableRequest.setFunctionAuthor(tableData.getFunctionAuthor());
        dmTableRequest.setRemark(tableData.getRemark());
        List<DmTableColumnRequest> columns = new ArrayList<>();
        for (DmLayoutTableRequest.PortItem tableItem : tableItems) {
            DmLayoutTableRequest.ColumnData portData = tableItem.getData();
            DmTableColumnRequest column = new DmTableColumnRequest();
            column.setDictId(portData.getDictId());
            column.setColumnName(portData.getColumnName());
            column.setColumnComment(portData.getColumnComment());
            column.setColumnType(portData.getColumnType());
            column.setIsPk(portData.getIsPk());
            column.setIsRequired(portData.getIsRequired());
            column.setDefaultValue(portData.getDefaultValue());
            columns.add(column);
        }
        dmTableRequest.setColumns(columns);
        dmTableRequest.setIndexes(tableData.getIndexes());
        applyTableLayout(dmTableRequest, isForced);
    }

    @Override
    public void applyTableLayout(DmTableRequest dmTableRequest, boolean isForced) {
        DmTable table = new DmTable();
        table.setDataSourceId(1L);
        table.setTableId(dmTableRequest.getTableId());
        table.setTableName(dmTableRequest.getTableName());
        table.setTableComment(dmTableRequest.getTableComment());
        table.setFunctionAuthor(dmTableRequest.getFunctionAuthor());
        table.setRemark(dmTableRequest.getRemark());

        boolean exists = dmTableMapper.exists(new LambdaQueryWrapper<DmTable>().eq(DmTable::getTableId, dmTableRequest.getTableId()));
        int row;
        if (exists) {
            row = dmTableMapper.updateById(table);
        } else {
            row = dmTableMapper.insert(table);
        }
        if (row > 0) {
            dmTableColumnMapper.delete(new LambdaQueryWrapper<DmTableColumn>().eq(DmTableColumn::getTableId, dmTableRequest.getTableId()));
            List<DmTableColumnRequest> columnsRequest = dmTableRequest.getColumns();
            List<DmTableColumn> saveColumns = new ArrayList<>();
            for (DmTableColumnRequest dmTableColumnRequest : columnsRequest) {
                DmTableColumn item = new DmTableColumn();
                item.setColumnName(dmTableColumnRequest.getColumnName());
                item.setColumnComment(dmTableColumnRequest.getColumnComment());
                item.setColumnType(dmTableColumnRequest.getColumnType());
                item.setIsPk(dmTableColumnRequest.getIsPk() ? "1" : "0");
                item.setIsRequired(dmTableColumnRequest.getIsRequired() ? "1" : "0");
                item.setDefaultValue(dmTableColumnRequest.getDefaultValue());
                item.setTableId(dmTableRequest.getTableId());
                item.setDictId(dmTableColumnRequest.getDictId());
                saveColumns.add(item);
            }
            if (CollUtil.isNotEmpty(saveColumns)) {
                dmTableColumnMapper.insertBatch(saveColumns);
            }
            dmTableIndexMapper.delete(new LambdaQueryWrapper<DmTableIndex>().eq(DmTableIndex::getTableId, dmTableRequest.getTableId()));
            dmTableIndexColumnMapper.delete(new LambdaQueryWrapper<DmTableIndexColumn>().eq(DmTableIndexColumn::getTableId, dmTableRequest.getTableId()));
            List<DmTableIndexRequest> indexes = dmTableRequest.getIndexes();

            if (CollUtil.isNotEmpty(indexes)) {
                List<DmTableIndexColumn> saveIndexColumns = new ArrayList<>();
                Map<String, Long> columnNameMap = saveColumns.stream().collect(Collectors.toMap(DmTableColumn::getColumnName, DmTableColumn::getColumnId));
                for (DmTableIndexRequest item : indexes) {
                    DmTableIndex genTableIndex = new DmTableIndex();
                    genTableIndex.setTableId(table.getTableId());
                    genTableIndex.setIndexName(item.getIndexName());
                    genTableIndex.setIndexType(item.getIndexType());
                    genTableIndex.setIsUnique(item.getIsUnique());
                    dmTableIndexMapper.insert(genTableIndex);

                    if (CollectionUtils.isNotEmpty(item.getColumnNameList())) {
                        for (String columnName : item.getColumnNameList()) {
                            DmTableIndexColumn genTableIndexColumn = new DmTableIndexColumn();
                            genTableIndexColumn.setTableId(table.getTableId());
                            genTableIndexColumn.setIndexId(genTableIndex.getIndexId());
                            genTableIndexColumn.setColumnId(columnNameMap.get(columnName));
                            saveIndexColumns.add(genTableIndexColumn);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(saveIndexColumns)) {
                    dmTableIndexColumnMapper.insertBatch(saveIndexColumns);
                }
            }
        }
        // 生成数据库表与索引
        createTable(dmTableRequest, isForced);
    }

    private void checkLayout(DmLayoutRequest layoutRequest) {
        if (StringUtils.isEmpty(layoutRequest.getTitle())) {
            throw new RuntimeException("请输入标题");
        }
    }

    private void createTable(DmTableRequest dmTableRequest, boolean isForced) {
        // 非强制执行需要验证是否存在数据
        if (!isForced) {
            int count = countTableRows(dmTableRequest.getTableName());
            if (count > 0) {
                throw new ServiceException("操作失败，该数据表存在数据", 50501);
            }
        }

        String dropSql = dropTableSql(dmTableRequest.getTableName());
        execute(dropSql);

        String sql = generateTableSql(dmTableRequest);
        execute(sql);

        List<String> indexSqlList = generateTableIndexSql(dmTableRequest);
        if (CollectionUtils.isNotEmpty(indexSqlList)) {
            for (String indexSql : indexSqlList) {
                execute(indexSql);
            }
        }
    }

    private Integer countTableRows(String tableName) {
        String sql = "SELECT COUNT(*) FROM `" + tableName + "`";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    private void execute(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return;
        }
        try {
            log.info("执行SQL: {}", sql);
            jdbcTemplate.execute(sql);
            log.info("成功执行SQL: {}", sql);
        } catch (Exception e) {
            log.error("执行SQL失败: {}", sql, e);
            throw new RuntimeException("执行SQL失败，原因：" + e.getCause().getMessage(), e);
        }
    }

    private String generateTableSql(DmTableRequest table) {
        StringBuilder columnSql = new StringBuilder();
        List<String> primaryKeys = new ArrayList<String>();
        for (DmTableColumnRequest column : table.getColumns()) {
            if (StringUtils.isEmpty(column.getColumnName())) {
                continue;
            }
            if (BooleanUtils.isTrue(column.getIsPk())) {
                primaryKeys.add(column.getColumnName());
            }
            columnSql.append("  `").append(column.getColumnName()).append("`");
            columnSql.append(" ").append(column.getColumnType());
            if (BooleanUtils.isTrue(column.getIsRequired())) {
                columnSql.append(" NOT NULL");
            }
            if (StringUtils.isNotEmpty(column.getDefaultValue())) {
                columnSql.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
            } else if (BooleanUtils.isNotTrue(column.getIsRequired())) {
                columnSql.append(" DEFAULT NULL");
            }
            if (StringUtils.isNotEmpty(column.getColumnComment())) {
                columnSql.append(" COMMENT '").append(column.getColumnComment()).append("'");
            }
            columnSql.append(",\n");
        }
        if (CollectionUtils.isNotEmpty(primaryKeys)) {
            columnSql.append("  PRIMARY KEY (");
            for (String primaryKey : primaryKeys) {
                columnSql.append("`").append(primaryKey).append("`,");
            }
            columnSql.setLength(columnSql.length() - 1);
            columnSql.append(")");
        } else {
            // 移除最后一个逗号和空格
            if (!columnSql.isEmpty()) {
                columnSql.setLength(columnSql.length() - ",\n".length());
            }
        }
        return String.format("""
            CREATE TABLE IF NOT EXISTS `%s` (
              %s
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='%s';
            """, table.getTableName(), columnSql, table.getTableComment());
    }

    private List<String> generateTableIndexSql(DmTableRequest table) {
        if (CollectionUtils.isEmpty(table.getIndexes())) {
            return null;
        }
        List<String> indexSqlList = new ArrayList<>();
        for (DmTableIndexRequest index : table.getIndexes()) {
            String indexType;
            if ("Full Text".equals(index.getIndexType())) {
                indexType = "FULLTEXT INDEX";
            } else {
                indexType = "INDEX";
            }
            indexSqlList.add(String.format("CREATE %s %s ON %s (%s)", indexType, index.getIndexName(), table.getTableName(), String.join(",", index.getColumnNameList())));
        }
        return indexSqlList;
    }

    private String dropTableSql(String tableName) {
        return String.format("DROP TABLE IF EXISTS %s", tableName);
    }


//    private String dropTableIndexSql(String tableName, List<String> indexNameList) {
//        List<String> indexSql = new ArrayList<>();
//        for (String indexName : indexNameList) {
//            indexSql.add(String.format("ALTER TABLE %s DROP INDEX %s", tableName, indexName));
//        }
//        return String.join(";", indexSql);
//    }
}
