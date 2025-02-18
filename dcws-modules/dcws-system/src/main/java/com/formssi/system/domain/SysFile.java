package com.formssi.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 表名：sys_file
 * 备注：文件存储表
 *
 * @author lizhangyu
 */
@Data
@TableName("sys_file")
public class SysFile {

    /** 
     * 对象存储主键
     */
    @TableId(value = "file_id")
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
     * 文件大小
     */
    private Long fileSize;

    /** 
     * 文件状态(0-生效，1-失效)
     */
    private Integer fileStatus;

    /** 
     * 存储类型(0-上传到服务器，1-上传到云服务oss)
     */
    private Integer storageType;

    /** 
     * md5唯一标识
     */
    private String identifier;

    /** 
     * 服务商
     */
    private String service;

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

}