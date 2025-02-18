package com.formssi.system.file;

import com.formssi.system.domain.vo.SysFileUploadVo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * oss 文件上传
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/6 10:40
 */
@Component
public class OssFileService extends AbstractFileService {

    @Override
    public SysFileUploadVo doUpload(MultipartFile file, String bucket, String objectName) throws Exception {
        return null;
    }

    @Override
    public SysFileUploadVo doUploadFile(MultipartFile file) throws Exception {
        return null;
    }

    @Override
    public void doDeleteFile(String objectName) throws Exception {

    }

}
