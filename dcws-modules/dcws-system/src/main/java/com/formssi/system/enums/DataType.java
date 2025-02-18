package com.formssi.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lizhangyu
 */
@AllArgsConstructor
@Getter
public enum DataType {

    ARRAY("array"),
    BINARY("binary"),
    FILE("file"),
    OBJECT("object"),
    ENUM("enum");

    private final String type;
}
