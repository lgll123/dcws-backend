package com.formssi.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.excel.utils.ExcelUtil;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.system.domain.SysModule;
import com.formssi.system.domain.bo.ModuleBo;
import com.formssi.system.domain.vo.ModuleVo;
import com.formssi.system.service.IModuleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author lizhangyu
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/module")
public class SysModuleController extends BaseController {

    private final IModuleService moduleService;

    /**
     * 获取项目模块
     * @param projectId 项目id
     * @return 返回所有模块
     */
    @GetMapping("/list")
    public R<List<ModuleVo>> listModule(Long projectId) {
        List<SysModule> modules = moduleService.listProjectModules(projectId);
        List<ModuleVo> moduleVos = CopyUtil.copyList(modules, ModuleVo::new);
        return R.ok(moduleVos);
    }

    /**
     * 查询项目模块列表
     */
    @SaCheckPermission("system:module:list")
    @GetMapping("/list2")
    public TableDataInfo<ModuleVo> list(ModuleBo bo, PageQuery pageQuery) {
        return moduleService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出项目模块列表
     */
    @SaCheckPermission("system:module:export")
    @Log(title = "项目模块", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ModuleBo bo, HttpServletResponse response) {
        List<ModuleVo> list = moduleService.queryList(bo);
        ExcelUtil.exportExcel(list, "项目模块", ModuleVo.class, response);
    }

    /**
     * 获取项目模块详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:module:query")
    @GetMapping("/{id}")
    public R<ModuleVo> getInfo(@NotNull(message = "主键不能为空")
                               @PathVariable Long id) {
        return R.ok(moduleService.queryById(id));
    }

    /**
     * 新增项目模块
     */
    @SaCheckPermission("system:module:add")
    @Log(title = "项目模块", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ModuleBo bo) {
        return toAjax(moduleService.insertByBo(bo));
    }

    /**
     * 修改项目模块
     */
    @SaCheckPermission("system:module:edit")
    @Log(title = "项目模块", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ModuleBo bo) {
        return toAjax(moduleService.updateByBo(bo));
    }

    /**
     * 删除项目模块
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:module:remove")
    @Log(title = "项目模块", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(moduleService.deleteWithValidByIds(List.of(ids), true));
    }

}
