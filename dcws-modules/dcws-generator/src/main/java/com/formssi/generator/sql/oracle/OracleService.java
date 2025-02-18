package com.formssi.generator.sql.oracle;

import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.SQLService;
import com.formssi.generator.sql.TableSelector;

public class OracleService implements SQLService {

	@Override
	public TableSelector getTableSelector(GeneratorConfig generatorConfig) {
		return new OracleTableSelector(new OracleColumnSelector(generatorConfig), generatorConfig);
	}

}
