package com.formssi.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.tenant.core.TenantEntity;
import lombok.Data;

import java.util.Date;

/**
 * 印章维护表 dcws_seal_info
 *
 * @author Lion Li
 */

@Data
@TableName("dcws_seal_info")
public class SealInfo extends TenantEntity {

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 印章名称
     */
    private String sealName;

    /**
     * 用印排序
     */
    private int sealSort;

    /**
     * 印章保管人
     */
    private Long sealUser;

    /**
     * 历史印章保管人
     */
    private String sealUserHis;

    /**
     * 备注
     */
    private String remark;

//    /**
//     * 创建部门
//     */
//    private Long createDept;
//
//    /**
//     * 创建者
//     */
//    private Long createBy;
//
//    /**
//     * 创建时间
//     */
//    private Date createTime;
//
//    /**
//     * 更新者
//     */
//    private Long updateBy;
//
//    /**
//     * 更新时间
//     */
//    private Date updateTime;
}
