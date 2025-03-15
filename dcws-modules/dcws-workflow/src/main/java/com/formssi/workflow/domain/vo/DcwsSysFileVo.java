package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.formssi.workflow.domain.DcwsSysFile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.util.Date;

/**
 * 备注：文件上传服务器记录存储表
 *
 * @author yqh
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsSysFile.class)
public class DcwsSysFileVo {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 
     * 对象存储主键
     */
    private Long Id;

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
     * 文件大小
     */
    private Long fileSize;

    /** 
     * 文件状态(0-生效，1-失效，2-待处理)
     */
    private Integer fileStatus;

    /** 
     * 是否已经存储到minio文件服务器 0:否 1:是 2:待上传
     */
    private Integer storageMinioServer;
    /**
     * 是否已经存储到档案管理系统0:失败 1:成功 2:上传失败待处理 3:上传中待重试 4:查询失败待重试 5:查询失败
     */
    private Integer storageDocumentServer;
    /**
     * 任务节点数据Id
     */
    private String taskNodeDataId;

    /** 
     * auto 处理次数
     */
    private String autoProcessNum;

    /** 
     * 创建时间
     */
    private Date createTime;

    /** 
     * 上传人
     */
    private Long createBy;

    /** 
     * 更新时间
     */
    private Date updateTime;

    /** 
     * 更新人
     */
    private Long updateBy;

    /**
     * 文件上传minio服务器成功或失败信息
     */
    private String minioMessage;

    /**
     * 文件上传档案系统服务器成功或失败信息
     */
    private String documentMessage;
    /**
     * 文件上传文件系统成功后的文件id
     */
    private String documentId;
    /**
     * 文件上传文件系统立马返回的查询ID
     */
    private String documentTaskId;

}