package com.formssi.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
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
import com.formssi.system.domain.vo.InfoChangeImportVo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.bo.TaskNodeDataQueryBo;
import com.formssi.workflow.domain.vo.DcwsInvoiceVo;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.domain.vo.TaskNodeDataHisVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.service.IApplyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
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
    @SaCheckPermission("workflow:apply:list")
    @GetMapping("/list")
    public TableDataInfo<TaskNodeDataVo> list(TaskNodeDataQueryBo bo, PageQuery pageQuery) {
        return applyService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询用印台账表单
     */
    @SaCheckPermission("workflow:apply:sealList")
    @GetMapping("/sealList")
    public TableDataInfo<TaskNodeDataVo> sealList(TaskNodeDataQueryBo bo, PageQuery pageQuery) {
        return applyService.queryPageSealList(bo, pageQuery);
    }

    /**
     * 导出申请列表
     */
    @SaCheckPermission("workflow:apply:export")
    @Log(title = "申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TaskNodeDataQueryBo bo, HttpServletResponse response) {
        List<TaskNodeDataVo> list = applyService.queryList(bo);
        ExcelUtil.exportExcel(list, "申请", TaskNodeDataVo.class, response);
    }

    /**
     * 获取申请详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("workflow:apply:query")
    @GetMapping("/{id}")
    public R<TaskNodeDataVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable String id) {
        return R.ok(applyService.queryById(id));
    }

    /**
     * 根据任务ID获取申请详细信息
     *
     * @param taskId 任务id
     */
    @SaCheckPermission("workflow:apply:query")
    @GetMapping("/getTaskNodeDataHisInfo")
    public R<TaskNodeDataHisVo> getTaskNodeDataInfo(@NotNull(message = "任务id不能为空") @PathVariable String taskId) {
        return R.ok(applyService.queryByTaskId(taskId));
    }

    /**
     * 新增申请
     */
    @SaCheckPermission("workflow:apply:add")
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
    @SaCheckPermission("workflow:apply:edit")
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
    @SaCheckPermission("workflow:apply:remove")
    @Log(title = "申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable String[] ids) {
        return toAjax(applyService.deleteWithValidByIds(List.of(ids)));
    }


    /**
     * 获取申请单PDFurl
     *
     * @param id 申请单号
     */
    @SaCheckPermission("workflow:apply:query")
    @GetMapping("/getApplyPDF")
    public R<DcwsSysFileVo> getApplyPDF(@NotNull(message = "申请单号不能为空") String id) {
        List<DcwsSysFileVo> applyPDFUrl = applyService.getApplyPDF(id);
        return R.ok(ObjectUtil.isEmpty(applyPDFUrl)?null:applyPDFUrl.get(0));
    }

    /**
     * 上传发票并识别发票信息
     *
     */
    @GetMapping("/uploadInvoice")
    public R<List<DcwsInvoiceVo>> uploadInvoice(@RequestParam(value = "fileIds") String fileIds) throws Exception {
        return R.ok(applyService.uploadInvoice(fileIds));
    }

    /**
     * 获取导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "档案信息", InfoChangeImportVo.class, response);
    }

}
