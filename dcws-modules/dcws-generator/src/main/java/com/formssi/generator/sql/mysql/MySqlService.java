package com.formssi.generator.sql.mysql;

import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.SQLService;
import com.formssi.generator.sql.TableSelector;

public class MySqlService implements SQLService {

	@Override
	public TableSelector getTableSelector(GeneratorConfig generatorConfig) {
		return new MySqlTableSelector(new MySqlColumnSelector(generatorConfig), generatorConfig);
	}

}
