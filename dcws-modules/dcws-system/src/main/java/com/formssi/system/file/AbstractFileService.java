package com.formssi.system.file;

import com.formssi.system.domain.vo.SysFileUploadVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/6 10:14
 */
public abstract class AbstractFileService {

    /**
     * 文件上传
     * @param file 文件
     * @param bucket 桶
     * @param objectName 文件名称
     * @return 返回上传结果
     */
    public abstract SysFileUploadVo doUpload(MultipartFile file, String bucket, String objectName) throws Exception;

    /**
     * 文件上传
     * @param file 文件
     * @return 返回上传结果
     */
    public abstract SysFileUploadVo doUploadFile(MultipartFile file) throws Exception;

    /**
     * 执行文件删除
     * @param objectName 文件名称
     */
    public abstract void doDeleteFile(String objectName) throws Exception;

}
