package com.formssi.system.domain.bo;

import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.system.domain.SysFile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件对象存储分页查询对象 sys_file
 *
 * @author lizhangyu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysFile.class, reverseConvertGenerate = false)
public class SysFileBo extends BaseEntity {

    /**
     * fileId
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
    private String url;

    /**
     * 服务商
     */
    private String service;

}
