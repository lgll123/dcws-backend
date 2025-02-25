package com.formssi.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * 查询物料库存
     */
    @SaCheckPermission("workflow:leave:list")
    @PostMapping("/accessories/query")
    public R<Map<String, Object>> query(@RequestBody @Validated AssetsSystemBo bo) {
        List<Map<String, Object>> maps = assetsSystemService.queryRepertory(bo);
        return R.ok(maps.isEmpty()?null:maps.get(0));
    }

    /**
     * 根据物料Id查询物料库存
     * categories：附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
     */
    @SaCheckPermission("workflow:leave:list")
    @GetMapping("/{categories}/queryQtyById")
    public R<AssetsSystemBo> queryQtyById(@NotBlank(message = "目录路径不能为空") @PathVariable String categories,AssetsSystemBo bo) {
        AssetsSystemBo assetsSystemBo = assetsSystemService.queryQtyById(bo,categories);

        return R.ok(assetsSystemBo);
    }

    /**
     * 根据目录类型查询目录列表
     * 目录类型：附属品-accessory、组件-component、许可证-license、消耗品-consumable、资产-asset、
     */
    @SaCheckPermission("workflow:leave:list")
    @GetMapping("/categories/{categoryType}")
    public TableDataInfo<CategoryBo> queryPageCategories(@NotBlank(message = "目录类型不能为空") @PathVariable String categoryType,
                                                         PageQuery pageQuery) {
        return assetsSystemService.queryPageCategories(categoryType,pageQuery,"categories");
    }

    /**
     * 根据目录id查询物料列表
     * categories：附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
     */
    @SaCheckPermission("workflow:leave:list")
    @GetMapping("/{categories}/{categoryId}")
    public TableDataInfo<AssetsSystemBo> queryAccessoriesById(@NotBlank(message = "目录路径不能为空") @PathVariable String categories,@NotNull(message = "目录Id不能为空") @PathVariable Integer categoryId, PageQuery pageQuery) {
        return assetsSystemService.queryAccessoriesById(categoryId,pageQuery,categories);
    }
}
