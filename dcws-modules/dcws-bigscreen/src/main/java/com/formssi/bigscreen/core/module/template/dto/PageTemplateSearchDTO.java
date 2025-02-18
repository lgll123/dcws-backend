package com.formssi.bigscreen.core.module.template.dto;

import com.formssi.bigscreen.core.constant.PageDesignConstant;
import com.formssi.common.dto.SearchDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hongyang
 * @version 1.0
 * @date 2023/3/20 16:38
 */
@Data
public class PageTemplateSearchDTO extends SearchDTO {

    /**
     * 参考：{@link PageDesignConstant.Type}
     */
    @ApiModelProperty(notes = "模板类型")
    private String type;

}
