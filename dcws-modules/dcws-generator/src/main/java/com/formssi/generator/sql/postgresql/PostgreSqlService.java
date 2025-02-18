package com.formssi.generator.sql.postgresql;

import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.SQLService;
import com.formssi.generator.sql.TableSelector;


/**
 * @author tanghc
 */
public class PostgreSqlService implements SQLService {

    @Override
    public TableSelector getTableSelector(GeneratorConfig generatorConfig) {
        return new PostgreSqlTableSelector(new PostgreSqlColumnSelector(generatorConfig), generatorConfig);
    }

}
