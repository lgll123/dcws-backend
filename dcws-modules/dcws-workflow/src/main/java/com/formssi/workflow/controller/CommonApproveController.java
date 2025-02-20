package com.formssi.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.workflow.domain.bo.DcwsApproveBo;
import com.formssi.workflow.domain.vo.DcwsApproveVo;
import com.formssi.workflow.service.CommonApproveService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/common/approve")
public class CommonApproveController extends BaseController {

    private final CommonApproveService commonApproveService;

    /**
     * 查询非标准流程列表
     */
    @SaCheckPermission("common:approve:list")
    @GetMapping("/list")
    public TableDataInfo<DcwsApproveVo> list(DcwsApproveBo bo, PageQuery pageQuery) {
        return commonApproveService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取非标准流程详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("common:approve:query")
    @GetMapping("/{id}")
    public R<DcwsApproveVo> getInfo(@NotNull(message = "主键不能为空")
                                  @PathVariable Long id) {
        return R.ok(commonApproveService.queryById(id));
    }

    /**
     * 新增非标准流程
     */
    @SaCheckPermission("common:approve:add")
    @Log(title = "非标准流程", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<DcwsApproveVo> add(@Validated(AddGroup.class) @RequestBody DcwsApproveBo bo) {
        return R.ok(commonApproveService.insertByBo(bo));
    }

    /**
     * 修改非标准流程
     */
    @SaCheckPermission("common:approve:edit")
    @Log(title = "非标准流程", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/edit")
    public R<DcwsApproveVo> edit(@Validated(EditGroup.class) @RequestBody DcwsApproveBo bo) {
        return R.ok(commonApproveService.updateByBo(bo));
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
        return toAjax(commonApproveService.cancelProcessApply(id));
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
        return toAjax(commonApproveService.deleteRunAndHisInstance(id));
    }
}
