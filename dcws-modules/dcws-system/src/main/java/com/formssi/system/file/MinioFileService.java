package com.formssi.system.file;

import com.formssi.common.minio.properties.MinioProperties;
import com.formssi.common.minio.util.MinioUtil;
import com.formssi.system.domain.vo.SysFileUploadVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * minio 文件上传
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/6 10:43
 */
@Component
public class MinioFileService extends AbstractFileService {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private MinioProperties minioProperties;

    @Override
    public SysFileUploadVo doUpload(MultipartFile file, String bucket, String objectName) throws Exception {
        minioUtil.createBucket(bucket);
        if (objectName != null) {
            minioUtil.uploadFile(file.getInputStream(), bucket, objectName + "/" + file.getOriginalFilename());
        } else {
            minioUtil.uploadFile(file.getInputStream(), bucket, file.getOriginalFilename());
        }

        SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
        sysFileUploadVo.setFileName(file.getOriginalFilename());

        // 获取永久访问URL
        String fileUrl = minioUtil.getPermanentTimePreviewUrl(bucket, file.getOriginalFilename());
        sysFileUploadVo.setUrl(fileUrl);
        // todo 待处理
        return null;
    }

    @Override
    public SysFileUploadVo doUploadFile(MultipartFile file) throws Exception {
        String bucketName = minioProperties.getBucketName();;

        // 文件上传
        minioUtil.uploadFile(file.getInputStream(), bucketName, file.getOriginalFilename());

        SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
        sysFileUploadVo.setFileName(file.getOriginalFilename());

        // 获取永久访问URL
        String fileUrl = minioUtil.getPermanentTimePreviewUrl(bucketName, file.getOriginalFilename());
        sysFileUploadVo.setUrl(fileUrl);

        return sysFileUploadVo;
    }

    @Override
    public void doDeleteFile(String objectName) throws Exception {
        String bucketName = minioProperties.getBucketName();;
        minioUtil.deleteObject(bucketName, objectName);
    }

}
