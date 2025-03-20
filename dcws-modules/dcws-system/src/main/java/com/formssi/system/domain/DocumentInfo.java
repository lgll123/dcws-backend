package com.formssi.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.tenant.core.TenantEntity;
import lombok.Data;

/**
 * 资料-部门维护表 dcws_document_info
 *
 * @author Lion Li
 */

@Data
@TableName("dcws_document_info")
public class DocumentInfo extends TenantEntity {

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
