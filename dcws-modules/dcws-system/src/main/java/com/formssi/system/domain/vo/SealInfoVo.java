package com.formssi.system.domain.vo;

import com.formssi.system.domain.SealInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 用户信息视图对象 sys_user
 *
 * @author Michelle.Chung
 */
@Data
@AutoMapper(target = SealInfo.class)
public class SealInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
