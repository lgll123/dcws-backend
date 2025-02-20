package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assets")
public class Assets extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 申请部门
     */
    private String applyDept;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 申请日期
     */
    private Date applyDate;

    /**
     * 物料使用人
     */
    private String checkTo;

    /**
     * 申请原因
     */
    private String applyReson;

    /**
     * 需求日期
     */
    private String requiredDate;


    /**
     * 物料名称
     */
    private String name;

    /**
     * 物料规格型号
     */
    private String modelNo;

    /**
     * 物料数量
     */
    private Integer number;

    /**
     * 物料使用地点
     */
    private String localtion;

    /**
     * 状态
     */
    private String status;


}
