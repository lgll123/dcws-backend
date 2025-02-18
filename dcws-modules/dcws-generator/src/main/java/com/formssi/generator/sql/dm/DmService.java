package com.formssi.generator.sql.dm;

import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.SQLService;
import com.formssi.generator.sql.TableSelector;

public class DmService implements SQLService {

	@Override
	public TableSelector getTableSelector(GeneratorConfig generatorConfig) {
		return new DmTableSelector(new DmColumnSelector(generatorConfig), generatorConfig);
	}

}
