package com.formssi.workflow.controller;

import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.DcwsNormalTaskBo;
import com.formssi.workflow.domain.vo.DcwsNormalTaskVo;
import com.formssi.workflow.domain.vo.DcwsTaskTypeVo;
import com.formssi.workflow.service.NormalTaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/common/approve")
public class NormalTaskController extends BaseController {

    private final NormalTaskService normalTaskService;

    /**
     * 查询非标准流程列表
     */
    @GetMapping("/list")
    public TableDataInfo<DcwsNormalTaskVo> list(DcwsNormalTaskBo bo, PageQuery pageQuery) {
        return normalTaskService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取非标准流程详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<DcwsNormalTaskVo> getInfo(@NotNull(message = "主键不能为空")  @PathVariable String id) {
        return R.ok(normalTaskService.queryById(id));
    }

    /**
     * 新增非标准流程
     */
    @Log(title = "非标准流程", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<DcwsNormalTaskVo> add(@Validated(AddGroup.class) @RequestBody DcwsNormalTaskBo bo) {
        return R.ok(normalTaskService.insertByBo(bo));
    }

    /**
     * 修改非标准流程
     */
    @Log(title = "非标准流程", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/edit")
    public R<DcwsNormalTaskVo> edit(@Validated(EditGroup.class) @RequestBody DcwsNormalTaskBo bo) {
        return R.ok(normalTaskService.updateByBo(bo));
    }

    /**
     * 撤销流程申请
     *
     * @param id 业务id
     */
    @Log(title = "非标准流程", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/cancelProcessApply/{id}")
    public R<Void> cancelProcessApply(@NotBlank(message = "业务id不能为空") @PathVariable String id) {
        return toAjax(normalTaskService.cancelProcessApply(id));
    }

    /**
     * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     *
     * @param id 业务id
     */
    @Log(title = "非标准流程", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @PostMapping("/deleteRunAndHisInstance/{id}")
    public R<Void> deleteRunAndHisInstance(@NotNull(message = "业务id不能为空") @PathVariable String id) {
        return toAjax(normalTaskService.deleteRunAndHisInstance(id));
    }

    /**
     * 导出模型zip压缩包
     *
     */
    @GetMapping("/queryWfType")
    public R<List<DcwsTaskTypeVo>> queryWfType() {
        return R.ok(normalTaskService.queryWfType());
    }
}
