package com.formssi.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 上传对象信息
 *
 * @author lizhangyu
 */
@Data
public class SysFileUploadVo {

    /**
     * URL地址
     */
    private String url;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 对象存储主键
     */
    private String fileId;



    /**
     * uid
     */
    @JsonIgnore
    private Long uid;

    /**
     * 状态
     */
    @JsonIgnore
    private String status;

}
