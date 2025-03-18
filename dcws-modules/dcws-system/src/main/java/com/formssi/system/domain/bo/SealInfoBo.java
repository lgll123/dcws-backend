package com.formssi.system.domain.bo;


import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.system.domain.SealInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息业务对象 sys_user
 *
 * @author Michelle.Chung
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SealInfo.class, reverseConvertGenerate = false)
public class SealInfoBo extends BaseEntity {

    /**
     * id
     */
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
