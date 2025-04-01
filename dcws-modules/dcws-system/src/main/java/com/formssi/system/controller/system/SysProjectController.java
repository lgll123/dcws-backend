package com.formssi.system.controller.system;

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
import com.formssi.system.domain.SysProject;
import com.formssi.system.domain.bo.ProjectBo;
import com.formssi.system.domain.vo.ProjectTreeVo;
import com.formssi.system.domain.vo.ProjectVo;
import com.formssi.system.service.IProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目信息
 *
 * @author tanghc
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
public class SysProjectController extends BaseController {

    private final IProjectService projectService;

    /**
     * 获取所有项目
     * @return 返回所有项目
     */
    @GetMapping("/list")
    public R<List<SysProject>> listProjectDoc() {
        List<SysProject> projectList = projectService.getAllProject();
        return R.ok(projectList);
    }

    /**
     * 查询项目列表
     */
    @SaCheckPermission("system:project:list")
    @GetMapping("/list2")
    public TableDataInfo<ProjectVo> list(ProjectBo bo, PageQuery pageQuery) {
        return projectService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出项目列表
     */
    @SaCheckPermission("system:project:export")
    @Log(title = "项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ProjectBo bo, HttpServletResponse response) {
        List<ProjectVo> list = projectService.queryList(bo);
        ExcelUtil.exportExcel(list, "项目", ProjectVo.class, response);
    }

    /**
     * 获取项目详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:project:query")
    @GetMapping("/{id}")
    public R<ProjectVo> getInfo(@NotNull(message = "主键不能为空")
                                @PathVariable Long id) {
        return R.ok(projectService.queryById(id));
    }

    /**
     * 新增项目
     */
    @SaCheckPermission("system:project:add")
    @Log(title = "项目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ProjectBo bo) {
        return toAjax(projectService.insertByBo(bo));
    }

    /**
     * 修改项目
     */
    @SaCheckPermission("system:project:edit")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ProjectBo bo) {
        return toAjax(projectService.updateByBo(bo));
    }

    /**
     * 删除项目
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:project:remove")
    @Log(title = "项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(projectService.deleteWithValidByIds(List.of(ids), true));
    }


    @SaCheckPermission({"system:project:list"})
    @GetMapping({"/tree"})
    public R<List<ProjectTreeVo>> tree() {
        List<ProjectTreeVo> result = this.projectService.tree();
        return R.ok(result);
    }
}