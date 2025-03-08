package com.formssi.workflow.controller;

import cn.hutool.core.util.ObjectUtil;
import com.formssi.common.core.domain.R;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.domain.bo.CategoryBo;
import com.formssi.workflow.externalsystem.assets.service.IAssetsSystemService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

/**
 * 资产系统接口
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/assetsSystem")
public class AssetsSystemController  extends BaseController {
    private final IAssetsSystemService assetsSystemService;
    /**
     * 根据物料Id查询物料库存
     * categories：附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
     */
    @GetMapping("/{categories}/queryQtyById")
    public R<AssetsSystemBo> queryQtyById(@NotBlank(message = "目录路径不能为空") @PathVariable String categories,AssetsSystemBo bo) {
        AssetsSystemBo assetsSystemBo = assetsSystemService.queryQtyById(bo,categories);
        return R.ok(assetsSystemBo);
    }

    /**
     * 根据目录类型查询目录列表
     * 目录类型：附属品-accessory、组件-component、许可证-license、消耗品-consumable、资产-asset、
     */
    @GetMapping("/categories/{categoryType}")
    public TableDataInfo<CategoryBo> queryPageCategories(@NotBlank(message = "目录类型不能为空") @PathVariable String categoryType,String applyType,
                                                         PageQuery pageQuery) {
        return assetsSystemService.queryPageCategories(categoryType,applyType,pageQuery,"categories");
    }

    /**
     * 根据目录id查询物料列表
     * categories：附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
     * assetStatus: 空：查询可分配列表 1：查询所有
     */
    @GetMapping("/{categories}/{categoryId}")
    public TableDataInfo<AssetsSystemBo> queryAccessoriesById(@NotBlank(message = "目录路径不能为空") @PathVariable String categories,
                                                              @NotNull(message = "目录Id不能为空") @PathVariable Integer categoryId,String assetStatus, PageQuery pageQuery) {
        return assetsSystemService.queryAccessoriesById(categoryId,assetStatus,pageQuery,categories);
    }

    /**
     * 查询组件可checkout的资产列表
     */
    @GetMapping("/hardware/selectlist")
    public Map<String, Object> selectlist(@NotNull(message = "page不能为空") Integer page) {
        return assetsSystemService.selectAssetslist(page,"hardware/selectlist");
    }

    /**
     * 文件上传到档案系统
     * @param uploadfile 上传文件
     * @param objectName 文件名称
     */
    @PostMapping(value = "/uploadfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> fileupload(@RequestParam MultipartFile uploadfile,
                                         @RequestParam(required = false) String objectName) throws Exception {
        if (ObjectUtil.isNull(uploadfile)) {
            return R.fail("上传文件不能为空");
        }

        // 文件上传
        String s = null;
        try {
            s = assetsSystemService.uploadDocument(
                    uploadfile,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } catch (IOException e) {
            return R.fail(e.getMessage());
        }

        return R.ok(s);
    }
}
