package com.formssi.system.controller.system;

import cn.hutool.core.util.ObjectUtil;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.QueryGroup;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.system.domain.bo.SysFileBo;
import com.formssi.system.domain.vo.SysFileUploadVo;
import com.formssi.system.domain.vo.SysFileVo;
import com.formssi.system.service.ISysFileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author ${context.author}
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/file")
public class SysFileController extends BaseController {

    private final ISysFileService sysFileService;

    /**
     * 文件上传
     * @param uploadfile 上传文件
     * @param bucket 桶
     * @param objectName 文件名称
     */
    @PostMapping(value = "/uploadfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFileUploadVo> fileupload(@RequestParam MultipartFile uploadfile, @RequestParam String bucket,
                                    @RequestParam(required = false) String objectName) throws Exception {
        if (ObjectUtil.isNull(uploadfile)) {
            return R.fail("上传文件不能为空");
        }

        // 文件上传
        SysFileUploadVo fileUploadVo = sysFileService.upload(uploadfile, bucket, objectName);

        return R.ok(fileUploadVo);
    }

    /**
     * 上传文件对象存储
     *
     * @param file 文件
     */
    @Log(title = "文件对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFileUploadVo> upload(@RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }

        SysFileUploadVo fileUploadVo = sysFileService.uploadFile(file);

        return R.ok(fileUploadVo);
    }

    /**
     * 查询文件对象存储列表
     * @param bo 请求条件bo
     * @param pageQuery 分页查询条件
     * @return 返回文件列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysFileVo> list(@Validated(QueryGroup.class) SysFileBo bo, PageQuery pageQuery) {
        return sysFileService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询文件对象基于id串
     *
     * @param fileIds 文件对象ID串
     */
    @GetMapping("/listByFileIds/{fileIds}")
    public R<List<SysFileVo>> listByFileIds(@NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] fileIds) {
        List<SysFileVo> list = sysFileService.listByFileIds(Arrays.asList(fileIds));
        return R.ok(list);
    }

    /**
     * 删除文件对象存储
     *
     * @param fileIds 文件对象ID串
     */
    @Log(title = "文件对象删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] fileIds) {
        return toAjax(sysFileService.deleteWithValidByIds(List.of(fileIds), true));
    }

    /**
     * 下载文件对象
     *
     * @param fileId 文件对象ID
     */
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
//        sysFileService.download(fileId, response);
    }

}
