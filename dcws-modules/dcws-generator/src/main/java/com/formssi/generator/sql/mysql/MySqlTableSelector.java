package com.formssi.generator.sql.mysql;


import com.formssi.generator.sql.ColumnSelector;
import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.TableDefinition;
import com.formssi.generator.sql.TableSelector;
import com.formssi.generator.util.FieldUtil;

import java.util.Map;

/**
 * 查询mysql数据库表
 */
public class MySqlTableSelector extends TableSelector {

	public MySqlTableSelector(ColumnSelector columnSelector,
							  GeneratorConfig dataBaseConfig) {
		super(columnSelector, dataBaseConfig);
	}

	@Override
	protected String getShowTablesSQL(GeneratorConfig generatorConfig) {
		String dbName = generatorConfig.getDbName();
		// 兼容dbName包含特殊字符会报错的情况
		if (!(dbName.startsWith("`") && dbName.endsWith("`"))) {
			dbName = String.format("`%s`",dbName);
		}
		String sql = "SHOW TABLE STATUS FROM " + dbName;
		if(this.getSchTableNames() != null && this.getSchTableNames().size() > 0) {
			StringBuilder tables = new StringBuilder();
			for (String table : this.getSchTableNames()) {
				tables.append(",'").append(table).append("'");
			}
			sql += " WHERE NAME IN (" + tables.substring(1) + ")";
		}
		return sql;
	}

	@Override
	protected TableDefinition buildTableDefinition(Map<String, Object> tableMap) {
		TableDefinition tableDefinition = new TableDefinition();
		tableDefinition.setTableName(FieldUtil.convertString(tableMap.get("NAME")));
		tableDefinition.setComment(FieldUtil.convertString(tableMap.get("COMMENT")));
		return tableDefinition;
	}

}
