package com.formssi.system.domain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.system.domain.DocumentInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author Michelle.Chung
 */
@Data
@AutoMapper(target = DocumentInfo.class)
public class DocumentInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 资料所属部门名称
     */
    private String deptName;

    /**
     * 提供资料人
     */
    private String providerUser;

    /**
     * 部门负责人
     */
    private Long leaderUser;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建部门
     */
    private Long createDept;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

}
