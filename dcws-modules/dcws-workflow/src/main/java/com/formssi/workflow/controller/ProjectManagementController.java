package com.formssi.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.DcwsProjectBo;
import com.formssi.workflow.domain.bo.DcwsProjectTaskBo;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.service.ProjectManagementService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/project/management")
public class ProjectManagementController extends BaseController {

    private final ProjectManagementService projectService;

    /**
     * 查询项目列表
     */
    @SaCheckPermission("project:management:list")
    @PostMapping("/list")
    public R<List<DcwsProjectVo>> list(@RequestBody DcwsProjectBo bo) {
        return R.ok(projectService.queryProjectList(bo));
    }

    /**
     * 查询项目下任务列表
     */
    @SaCheckPermission("project:management:list")
    @PostMapping("/tasklist")
    public R<List<DcwsProjectTaskVo>> tasklist(@RequestBody DcwsProjectTaskBo bo) {
        return R.ok(projectService.queryProjectTaskList(bo));
    }

    /**
     * 获取项目详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("project:management:query")
    @GetMapping("/{id}")
    public R<DcwsProjectVo> getInfo(@NotNull(message = "主键不能为空")  @PathVariable Long id) {
        return R.ok(projectService.queryById(id));
    }

    /**
     * 新增项目
     */
    @SaCheckPermission("project:management:add")
    @Log(title = "项目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<DcwsProjectVo> add(@Validated(AddGroup.class) @RequestBody DcwsProjectBo bo) {
        return R.ok(projectService.insertByBo(bo));
    }

    /**
     * 修改项目
     */
    @SaCheckPermission("project:management:edit")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/edit")
    public R<DcwsProjectVo> edit(@Validated(EditGroup.class) @RequestBody DcwsProjectBo bo) {
        return R.ok(projectService.updateByBo(bo));
    }

    /**
     * 修改任务
     */
    @SaCheckPermission("project:management:edit")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/edittask")
    public R<DcwsProjectTaskVo> edittask(@Validated(EditGroup.class) @RequestBody DcwsProjectTaskBo bo) {
        return R.ok(projectService.updateByTaskBo(bo));
    }

    /**
     * 查询任务归属
     */
    @SaCheckPermission("project:management:edit")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/querytaskbelonging")
    public R<TaskVo> querytaskbelonging(@Validated(EditGroup.class) @RequestBody DcwsProjectTaskBo bo) {
        TaskVo TaskVo = projectService.querytaskbelonging(bo);
        return R.ok(TaskVo);
    }
}
