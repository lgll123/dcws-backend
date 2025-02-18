package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.SysFile;
import com.formssi.system.domain.bo.SysFileBo;
import com.formssi.system.domain.vo.SysFileUploadVo;
import com.formssi.system.domain.vo.SysFileVo;
import com.formssi.system.domain.vo.SysOssVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/4 16:22
 */
public interface ISysFileService extends IService<SysFile> {

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param file 要上传的 MultipartFile 对象
     * @param bucket 桶
     * @param objectName 文件名称
     * @return 上传成功后的 SysFileUploadVo 对象，包含文件信息
     */
    SysFileUploadVo upload(MultipartFile file, String bucket, String objectName);

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param file 要上传的 MultipartFile 对象
     * @return 上传成功后的 SysFileUploadVo 对象，包含文件信息
     */
    SysFileUploadVo uploadFile(MultipartFile file);

    /**
     * 查询文件对象存储列表
     *
     * @param sysFileBo 文件对象存储分页查询对象
     * @param pageQuery 分页查询实体类
     * @return 结果
     */
    TableDataInfo<SysFileVo> queryPageList(SysFileBo sysFileBo, PageQuery pageQuery);

    /**
     * 根据文件id列表获取文件
     * @param fileIds 文件id
     * @return 返回文件列表
     */
    List<SysFileVo> listByFileIds(List<Long> fileIds);

    /**
     * 删除文件对象存储
     *
     * @param ids     文件对象ID串
     * @param isValid 判断是否需要校验
     * @return 结果
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
