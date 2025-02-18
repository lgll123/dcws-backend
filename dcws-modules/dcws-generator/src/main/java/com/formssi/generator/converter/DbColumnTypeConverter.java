package com.formssi.generator.converter;


import com.formssi.generator.domain.TypeConfig;

import java.util.Map;

/**
 * @author thc
 */
public class DbColumnTypeConverter implements ColumnTypeConverter {


    private static TypeConfig DEF = new TypeConfig();
    static {
        DEF.setBaseType("String");
        DEF.setBoxType("String");
    }

    // db_type
    private final Map<String, TypeConfig> typeMap;

    public DbColumnTypeConverter(Map<String, TypeConfig> typeMap) {
        this.typeMap = typeMap;
    }

    @Override
    public String convertType(String type) {
        return typeMap.getOrDefault(type, DEF).getBaseType();
    }

    @Override
    public String convertTypeBox(String type) {
        return typeMap.getOrDefault(type, DEF).getBoxType();
    }

}
