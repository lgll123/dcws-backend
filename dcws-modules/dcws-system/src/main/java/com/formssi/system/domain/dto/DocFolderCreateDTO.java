package com.formssi.system.domain.dto;

import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.system.domain.dataid.DocInfoDataId;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.enums.DocTypeEnum;
import lombok.Data;

import java.util.Map;

/**
 * @author tanghc
 */
@Data
public class DocFolderCreateDTO implements DocInfoDataId {
    private Long moduleId;

    private String name;

    private Long parentId;

    /** 维护人, 数据库字段：author */
    private String author;

    private Integer orderIndex;

    private DocTypeEnum docTypeEnum;

    private Map<String, ?> props;

    private LoginUser loginUser;

    @Override
    public Byte getIsFolder() {
        return BooleanEnum.TRUE.getType();
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String getHttpMethod() {
        return "";
    }
}
