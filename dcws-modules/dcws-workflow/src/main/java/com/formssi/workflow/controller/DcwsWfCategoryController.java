package com.formssi.workflow.controller;

import com.formssi.workflow.domain.bo.WfCategoryBo;
import com.formssi.workflow.domain.vo.WfCategoryVo;
import com.formssi.workflow.service.IWfCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.excel.utils.ExcelUtil;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程分类
 *
 * @author may
 * @date 2023-06-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/categorydcws")
public class DcwsWfCategoryController extends BaseController {

    private final IWfCategoryService wfCategoryService;

    /**
     * 查询流程分类列表
     */
    @GetMapping("/list")
    public R<List<WfCategoryVo>> list(WfCategoryBo bo) {
        List<WfCategoryVo> list = wfCategoryService.queryList(bo);
        return R.ok(list);

    }

    /**
     * 导出流程分类列表
     */
    @Log(title = "流程分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(WfCategoryBo bo, HttpServletResponse response) {
        List<WfCategoryVo> list = wfCategoryService.queryList(bo);
        ExcelUtil.exportExcel(list, "流程分类", WfCategoryVo.class, response);
    }

    /**
     * 获取流程分类详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<WfCategoryVo> getInfo(@NotNull(message = "主键不能为空")
                                   @PathVariable Long id) {
        return R.ok(wfCategoryService.queryById(id));
    }

    /**
     * 新增流程分类
     */
    @Log(title = "流程分类", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WfCategoryBo bo) {
        return toAjax(wfCategoryService.insertByBo(bo));
    }

    /**
     * 修改流程分类
     */
    @Log(title = "流程分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WfCategoryBo bo) {
        return toAjax(wfCategoryService.updateByBo(bo));
    }

    /**
     * 删除流程分类
     *
     * @param ids 主键串
     */
    @Log(title = "流程分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(wfCategoryService.deleteWithValidByIds(List.of(ids), true));
    }
}
