package com.formssi.workflow.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.Assets;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 物料申请业务对象 assets
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Assets.class, reverseConvertGenerate = false)
public class AssetsBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 申请部门
     */
    @NotBlank(message = "申请部门不能为空", groups = {AddGroup.class, EditGroup.class})
    private String applyDept;

    /**
     * 申请人
     */
    @NotBlank(message = "申请人不能为空", groups = {AddGroup.class, EditGroup.class})
    private String applicant;

    /**
     * 申请日期
     */
    @NotNull(message = "申请日期不能为空", groups = {AddGroup.class, EditGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date applyDate;

    /**
     * 物料使用人
     */
    @NotBlank(message = "物料使用人不能为空", groups = {AddGroup.class, EditGroup.class})
    private String checkTo;

    /**
     * 申请原因
     */
    @NotBlank(message = "申请原因不能为空", groups = {AddGroup.class, EditGroup.class})
    private String applyReson;

    /**
     * 需求日期
     */
    @NotBlank(message = "申请原因不能为空", groups = {AddGroup.class, EditGroup.class})
    private String requiredDate;

    /**
     * 物料名称
     */
    @NotBlank(message = "物料名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /**
     * 物料规格型号
     */
    @NotBlank(message = "物料规格型号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String modelNo;

    /**
     * 物料数量
     */
    @NotNull(message = "物料数量不能为空", groups = {AddGroup.class, EditGroup.class})
    private Integer number;

    /**
     * 物料使用地点
     */
    @NotBlank(message = "物料使用地点不能为空", groups = {AddGroup.class, EditGroup.class})
    private String localtion;



    /**
     * 状态
     */
    private String status;


}
