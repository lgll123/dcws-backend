package com.formssi.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.excel.utils.ExcelUtil;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.AssetsBo;
import com.formssi.workflow.domain.vo.AssetsVo;
import com.formssi.workflow.service.IAssetsService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料申请
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/assets")
public class AssetsController extends BaseController {

    private final IAssetsService assetsService;

    /**
     * 查询物料申请列表
     */
    @SaCheckPermission("workflow:leave:list")
    @GetMapping("/list")
    public TableDataInfo<AssetsVo> list(AssetsBo bo, PageQuery pageQuery) {
        return assetsService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出物料申请列表
     */
    @SaCheckPermission("workflow:leave:export")
    @Log(title = "物料申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AssetsBo bo, HttpServletResponse response) {
        List<AssetsVo> list = assetsService.queryList(bo);
        ExcelUtil.exportExcel(list, "物料申请", AssetsVo.class, response);
    }

    /**
     * 获取物料申请详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("workflow:leave:query")
    @GetMapping("/{id}")
    public R<AssetsVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(assetsService.queryById(id));
    }

    /**
     * 新增物料申请
     */
    @SaCheckPermission("workflow:leave:add")
    @Log(title = "物料申请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<AssetsVo> add(@Validated(AddGroup.class) @RequestBody AssetsBo bo) {
        return R.ok(assetsService.insertByBo(bo));
    }

    /**
     * 修改物料申请
     */
    @SaCheckPermission("workflow:leave:edit")
    @Log(title = "物料申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<AssetsVo> edit(@Validated(EditGroup.class) @RequestBody AssetsBo bo) {
        return R.ok(assetsService.updateByBo(bo));
    }

    /**
     * 删除物料申请
     *
     * @param ids 主键串
     */
    @SaCheckPermission("workflow:leave:remove")
    @Log(title = "物料申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(assetsService.deleteWithValidByIds(List.of(ids)));
    }
}
