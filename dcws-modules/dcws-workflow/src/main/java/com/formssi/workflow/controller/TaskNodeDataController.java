package com.formssi.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSON;
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
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.vo.TaskNodeDataHisVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.service.IApplyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 申请
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/apply")
public class TaskNodeDataController extends BaseController {

    private final IApplyService applyService;

    /**
     * 查询申请列表
     */
    @SaCheckPermission("workflow:leave:list")
    @GetMapping("/list")
    public TableDataInfo<TaskNodeDataVo> list(TaskNodeDataBo bo, PageQuery pageQuery) {
        return applyService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出申请列表
     */
    @SaCheckPermission("workflow:leave:export")
    @Log(title = "申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TaskNodeDataBo bo, HttpServletResponse response) {
        List<TaskNodeDataVo> list = applyService.queryList(bo);
        ExcelUtil.exportExcel(list, "申请", TaskNodeDataVo.class, response);
    }

    /**
     * 获取申请详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("workflow:leave:query")
    @GetMapping("/{id}")
    public R<TaskNodeDataVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(applyService.queryById(id));
    }

    /**
     * 根据任务ID获取申请详细信息
     *
     * @param taskId 任务id
     */
    @SaCheckPermission("workflow:leave:query")
    @GetMapping("/getTaskNodeDataHisInfo")
    public R<TaskNodeDataHisVo> getTaskNodeDataInfo(@NotNull(message = "任务id不能为空") @PathVariable String taskId) {
        return R.ok(applyService.queryByTaskId(taskId));
    }

    /**
     * 新增申请
     */
    @SaCheckPermission("workflow:leave:add")
    @Log(title = "申请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<TaskNodeDataVo> add(@Validated(AddGroup.class) @RequestBody TaskNodeDataBo bo) {
      /*  try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode attributes = objectMapper.readTree("{\"color\":\"red\", \"size\":10}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
*/
        String jsonString = JSON.toJSONString("{\"color\":\"red\", \"size\":10}"); // TODO yqh
        return R.ok(applyService.insertByBo(bo));
    }

    /**
     * 修改申请
     */
    @SaCheckPermission("workflow:leave:edit")
    @Log(title = "申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<TaskNodeDataVo> edit(@Validated(EditGroup.class) @RequestBody TaskNodeDataBo bo) {
        return R.ok(applyService.updateByBo(bo));
    }

    /**
     * 删除申请
     *
     * @param ids 主键串
     */
    @SaCheckPermission("workflow:leave:remove")
    @Log(title = "申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(applyService.deleteWithValidByIds(List.of(ids)));
    }
}
