package com.formssi.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.oss.core.OssClient;
import com.formssi.common.oss.factory.OssFactory;
import com.formssi.system.domain.SysFile;
import com.formssi.system.domain.SysOss;
import com.formssi.system.domain.bo.SysFileBo;
import com.formssi.system.domain.vo.SysFileUploadVo;
import com.formssi.system.domain.vo.SysFileVo;
import com.formssi.system.enums.FileStatusEnum;
import com.formssi.system.enums.FileStorageTypeEnum;
import com.formssi.system.enums.FileUploadTypeEnum;
import com.formssi.system.file.AbstractFileService;
import com.formssi.system.mapper.SysFileMapper;
import com.formssi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/4 16:22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    @Value("${file.upload.type}")
    private String fileUploadType;

    private final IdentifierGenerator identifierGenerator;

    @Override
    public SysFileUploadVo upload(MultipartFile file, String bucket, String objectName) {
        // 获取具体实现上传实现类
        AbstractFileService target = getFileService();
        SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
        try {
            // 文件上传
            sysFileUploadVo = target.doUpload(file, bucket, objectName);
        }catch (Exception e) {
            throw new ServiceException("文件上传异常，异常信息为:" + e.getMessage());
        }

        // 插入数据
        insertUploadResult(sysFileUploadVo, file.getOriginalFilename());

        return sysFileUploadVo;
    }

    @Override
    public SysFileUploadVo uploadFile(MultipartFile file) {
        // 获取具体实现类
        AbstractFileService target = getFileService();
        SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
        try {
            // 文件上传
            sysFileUploadVo = target.doUploadFile(file);
        }catch (Exception e) {
            throw new ServiceException("文件上传异常，异常信息为:" + e.getMessage());
        }

        // 插入数据
        insertUploadResult(sysFileUploadVo, file.getOriginalFilename());

        return sysFileUploadVo;
    }

    @Override
    public TableDataInfo<SysFileVo> queryPageList(SysFileBo sysFileBo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysFile> lqw = buildQueryWrapper(sysFileBo);
        Page<SysFileVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        result.setRecords(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public List<SysFileVo> listByFileIds(List<Long> fileIds) {
        List<SysFile> sysFileList = getListByFileIdList(fileIds);
        return CopyUtil.copyList(sysFileList, SysFileVo::new);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        List<SysFile> list = baseMapper.selectBatchIds(ids);

        // 获取具体实现类
        AbstractFileService target = getFileService();

        // 服务器上文件删除
        try {
            for (SysFile sysFile : list) {
                target.doDeleteFile(sysFile.getFileName());
            }
        }catch (Exception e) {
            throw new ServiceException("文件删除异常，异常信息为:" + e.getMessage());
        }

        // 数据库文件删除
        return baseMapper.deleteByIds(ids) > 0;
    }

    private List<SysFile> getListByFileIdList(List<Long> fileIdList) {
        if (CollectionUtils.isEmpty(fileIdList)) {
            return Collections.emptyList();
        }

        return lambdaQuery().in(SysFile::getFileId, fileIdList).list();
    }

    /**
     * 构建query查询
     * @param bo 请求参数
     * @return 返回LambdaQueryWrapper
     */
    private LambdaQueryWrapper<SysFile> buildQueryWrapper(SysFileBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysFile> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), SysFile::getFileName, bo.getFileName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), SysFile::getOriginalName, bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), SysFile::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), SysFile::getFileUrl, bo.getUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
                SysFile::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.eq(ObjectUtil.isNotNull(bo.getCreateBy()), SysFile::getCreateBy, bo.getCreateBy());
        lqw.eq(StringUtils.isNotBlank(bo.getService()), SysFile::getService, bo.getService());
        lqw.orderByDesc(SysFile::getCreateTime);
        return lqw;
    }

    /**
     * 插入数据
     * @param sysFileUploadVo 上传文件
     */
    private void insertUploadResult(SysFileUploadVo sysFileUploadVo, String originalName) {
        String fileName = sysFileUploadVo.getFileName();
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setFileUrl(sysFileUploadVo.getUrl());
        sysFile.setFileStatus(FileStatusEnum.EFFECTIVE.getStatus());
        sysFile.setOriginalName(originalName);
        if (fileUploadType.equals(FileUploadTypeEnum.OSS.getType())) {
            sysFile.setStorageType(FileStorageTypeEnum.CLOUD_SERVER.getType());
        }else {
            sysFile.setStorageType(FileStorageTypeEnum.SERVER.getType());
        }

        // 获取文件后缀
        String suffix = StringUtils.substring(fileName, fileName.lastIndexOf("."), fileName.length());
        sysFile.setFileSuffix(suffix);

        // 设置md5唯一标识
        String identifier = identifierGenerator.nextId(null).toString();
        sysFile.setIdentifier(identifier);
        // 创建者设置为管理员
        sysFile.setCreateBy(1L);

        // 插入数据
        baseMapper.insert(sysFile);

        sysFileUploadVo.setFileId(sysFile.getFileId().toString());
    }

    /**
     * 获取文件上传实现类
     * @return 返回文件实现类
     */
    private AbstractFileService getFileService() {
        String name = FileUploadTypeEnum.getEnumByType(fileUploadType).getName();
        return SpringUtils.getBean(name, AbstractFileService.class);
    }

}
