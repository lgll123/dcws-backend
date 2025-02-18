package com.formssi.generator.sql.sqlserver;

import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.SQLService;
import com.formssi.generator.sql.TableSelector;

public class SqlServerService implements SQLService {

	@Override
	public TableSelector getTableSelector(GeneratorConfig generatorConfig) {
		return new SqlServerTableSelector(new SqlServerColumnSelector(generatorConfig), generatorConfig);
	}

}
