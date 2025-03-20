package com.formssi.system.domain.vo;

import com.formssi.common.translation.annotation.Translation;
import com.formssi.common.translation.constant.TransConstant;
import com.formssi.system.domain.SysFile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件对象存储视图对象 sys_file
 *
 * @author lizhangyu
 */
@Data
@AutoMapper(target = SysFile.class)
public class SysFileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象存储主键
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原名
     */
    private String originalName;

    /**
     * 文件后缀名
     */
    private String fileSuffix;

    /**
     * URL地址
     */
    private String fileUrl;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上传人
     */
    private Long createBy;

    /**
     * 上传人名称
     */
    @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "createBy")
    private String createByName;

    /**
     * 服务商
     */
    private String service;

    /**
     * 关联文件id
     */
    private Long associationFileId;

}
