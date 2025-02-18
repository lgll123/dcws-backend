package com.formssi.bigscreen.core.module.file.controller;

import com.formssi.common.permission.ApiPermission;
import com.formssi.bigscreen.core.module.file.dto.FileSearchDTO;
import com.formssi.bigscreen.core.module.file.entity.DataRoomFileEntity;
import com.formssi.bigscreen.core.module.file.service.IDataRoomFileService;
import com.formssi.bigscreen.core.module.file.service.IDataRoomOssService;
import com.formssi.bigscreen.core.module.file.vo.DataRoomFileVO;
import com.formssi.common.controller.SuperController;
import com.formssi.common.utils.BeanConvertUtils;
import com.formssi.common.vo.PageVO;
import com.formssi.common.vo.R;
import com.formssi.bigscreen.core.permission.Permission;
import io.swagger.annotations.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 文件管理
 */
@Slf4j
@RestController
@RequestMapping("/bigScreen/file")
@Api(tags = "文件管理")
@ApiSort(value = 100)
public class DataRoomFileController extends SuperController {

    @Resource
    private IDataRoomOssService sysOssService;
    @Resource
    private IDataRoomFileService fileService;

    @ApiPermission(permissions = {Permission.File.VIEW})
    @GetMapping(value = {"", "/"})
    @ApiOperation(value = "列表", position = 10, notes = "分页查询文件", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页条数", paramType = "query", required = true, dataType = "int"),
            @ApiImplicitParam(name = "searchKey", value = "查询条件", paramType = "query", dataType = "string")
    })
    public R<PageVO<DataRoomFileVO>> getPage(@ApiParam(name = "查询", value = "传入查询的业务条件", required = true) FileSearchDTO searchDTO) {
        PageVO<DataRoomFileEntity> page = fileService.getPage(searchDTO);
        PageVO<DataRoomFileVO> pageVO = BeanConvertUtils.convertPage(page, DataRoomFileVO.class);
        return R.success(pageVO);
    }

    @ApiPermission(permissions = {Permission.File.UPLOAD})
    @PostMapping("/upload")
    @ApiOperation(value = "上传", notes = "上传", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<DataRoomFileEntity> upload(@RequestParam("file") MultipartFile file, @RequestParam(value = "module", required = false) String module, HttpServletResponse response, HttpServletRequest request) {
        DataRoomFileEntity entity = new DataRoomFileEntity();
        // 不同业务自己控制
        if (StringUtils.isBlank(module)) {
            module = "other";
        }
        entity.setModule(module);
        sysOssService.upload(file, entity, response, request);
        fileService.save(entity);
        return R.success(entity);
    }

    @ApiPermission(permissions = {Permission.File.DOWNLOAD})
    @PostMapping("/download/{id}")
    @ApiOperation(value = "下载", notes = "下载资源", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void download(@PathVariable("id") String id, HttpServletResponse response, HttpServletRequest request) {
        sysOssService.download(id, response, request);
    }

    @ApiPermission(permissions = {Permission.File.VIEW})
    @GetMapping("/getAllFileSuffix")
    @ApiOperation(value = "获取所有文件后缀名", notes = "获取所有文件后缀名", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<List<String>> getAllFileSuffix() {
    	List<String> extensions = fileService.getAllExtension();
        // 移除空后缀
        extensions.remove("");
    	return R.success(extensions);
    }

    @ApiPermission(permissions = {Permission.File.DELETE})
    @PostMapping("/delete/{id}")
    @ApiOperation(value = "删除", notes = "删除", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<Boolean> delete(@PathVariable("id") String id) {
        sysOssService.delete(id);
        return R.success(true);
    }
}
