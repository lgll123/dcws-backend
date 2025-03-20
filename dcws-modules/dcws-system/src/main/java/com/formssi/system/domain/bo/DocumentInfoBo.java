package com.formssi.system.domain.bo;


import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.system.domain.DocumentInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Michelle.Chung
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DocumentInfo.class, reverseConvertGenerate = false)
public class DocumentInfoBo extends BaseEntity {

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
     * 默认部门（1是 0否  发展部是默认部门）
     */
    private String isDeaultDept;

}
