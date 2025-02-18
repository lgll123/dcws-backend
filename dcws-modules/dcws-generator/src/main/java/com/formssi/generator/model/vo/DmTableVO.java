package com.formssi.generator.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.formssi.generator.domain.DmTableColumn;
import com.formssi.generator.model.request.DmTableIndexRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 表 返回参数
 *
 * @author Shen Tao
 */
@Data
public class DmTableVO {

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
     * 实体类名称(首字母大写)
     */
    @NotBlank(message = "实体类名称不能为空")
    private String className;

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
    private List<DmTableColumn> columns;

    /**
     * 索引信息
     */
    @Valid
    @TableField(exist = false)
    private List<DmTableIndexRequest> indexes;

//
//    /**
//     * 使用的模板（crud单表操作 tree树表操作 sub主子表操作）
//     */
//    private String tplCategory;
//
//    /**
//     * 生成包路径
//     */
//    @NotBlank(message = "生成包路径不能为空")
//    private String packageName;
//
//    /**
//     * 生成模块名
//     */
//    @NotBlank(message = "生成模块名不能为空")
//    private String moduleName;
//
//    /**
//     * 生成业务名
//     */
//    @NotBlank(message = "生成业务名不能为空")
//    private String businessName;
//
//    /**
//     * 生成功能名
//     */
//    @NotBlank(message = "生成功能名不能为空")
//    private String functionName;

}
