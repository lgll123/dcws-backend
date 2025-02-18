package com.formssi.generator.model.request;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 表 请求参数
 *
 * @author Shen Tao
 */
@Data
public class DmTableRequest {

    private Long tableId;

    /**
     * 表名称
     */
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 表描述
     */
    @NotBlank(message = "表描述不能为空")
    private String tableComment;

    /**
     * 生成作者
     */
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;

    /**
     * 备注
     */
    private String remark;

    /**
     * 表列信息
     */
    @Valid
    @TableField(exist = false)
    private List<DmTableColumnRequest> columns;

    /**
     * 索引信息
     */
    @Valid
    @TableField(exist = false)
    private List<DmTableIndexRequest> indexes;

}
