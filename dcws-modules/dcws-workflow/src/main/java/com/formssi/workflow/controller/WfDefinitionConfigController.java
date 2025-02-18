package com.formssi.workflow.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import com.formssi.workflow.domain.bo.WfDefinitionConfigBo;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.formssi.common.idempotent.annotation.RepeatSubmit;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.web.core.BaseController;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.workflow.domain.vo.WfDefinitionConfigVo;
import com.formssi.workflow.service.IWfDefinitionConfigService;

/**
 * 流程定义配置
 *
 * @author may
 * @date 2024-03-18
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/definitionConfig")
public class WfDefinitionConfigController extends BaseController {

    private final IWfDefinitionConfigService wfDefinitionConfigService;


    /**
     * 获取流程定义配置详细信息
     *
     * @param definitionId 主键
     */
    @GetMapping("/getByDefId/{definitionId}")
    public R<WfDefinitionConfigVo> getByDefId(@NotBlank(message = "流程定义ID不能为空")
                                              @PathVariable String definitionId) {
        return R.ok(wfDefinitionConfigService.getByDefId(definitionId));
    }

    /**
     * 新增流程定义配置
     */
    @Log(title = "流程定义配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/saveOrUpdate")
    public R<Void> saveOrUpdate(@Validated(AddGroup.class) @RequestBody WfDefinitionConfigBo bo) {
        return toAjax(wfDefinitionConfigService.saveOrUpdate(bo));
    }

    /**
     * 删除流程定义配置
     *
     * @param ids 主键串
     */
    @Log(title = "流程定义配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(wfDefinitionConfigService.deleteByIds(List.of(ids)));
    }

    /**
     * 查询流程定义配置排除当前查询的流程定义
     *
     * @param tableName    表名
     * @param definitionId 流程定义id
     */
    @GetMapping("/getByTableNameNotDefId/{tableName}/{definitionId}")
    public R<List<WfDefinitionConfigVo>> getByTableNameNotDefId(@NotBlank(message = "表名不能为空") @PathVariable String tableName,
                                                                @NotBlank(message = "流程定义ID不能为空") @PathVariable String definitionId) {
        return R.ok(wfDefinitionConfigService.getByTableNameNotDefId(tableName, definitionId));
    }

}
