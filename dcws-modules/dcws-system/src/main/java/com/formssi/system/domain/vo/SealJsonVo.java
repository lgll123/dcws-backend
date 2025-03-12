package com.formssi.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formssi.system.domain.SealInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 用户信息视图对象 sys_user
 *
 * @author Michelle.Chung
 */
@Data
@AutoMapper(target = SealInfo.class)
public class SealJsonVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 印章名称
     */
    @JsonIgnore
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

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 份数
     */
    private String applyNum;

    /**
     * 文件
     */
    private List<SysFileUploadVo> sealFile;

    /**
     * 印章list
     */
    private List<SealInfo> sealInfoList;

}
