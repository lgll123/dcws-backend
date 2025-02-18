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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import com.formssi.workflow.domain.bo.ModelBo;
import com.formssi.workflow.domain.vo.ModelVo;
import com.formssi.workflow.service.IActModelService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 模型管理 控制层
 *
 * @author may
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/model")
public class ActModelController extends BaseController {

    @Autowired(required = false)
    private RepositoryService repositoryService;
    private final IActModelService actModelService;


    /**
     * 分页查询模型
     *
     * @param modelBo 模型参数
     */
    @GetMapping("/list")
    public TableDataInfo<Model> page(ModelBo modelBo, PageQuery pageQuery) {
        return actModelService.page(modelBo, pageQuery);
    }

    /**
     * 新增模型
     *
     * @param modelBo 模型请求对象
     */
    @Log(title = "模型管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/save")
    public R<Void> saveNewModel(@Validated(AddGroup.class) @RequestBody ModelBo modelBo) {
        return toAjax(actModelService.saveNewModel(modelBo));
    }

    /**
     * 查询模型
     *
     * @param id 模型id
     */
    @GetMapping("/getInfo/{id}")
    public R<ModelVo> getInfo(@NotBlank(message = "模型id不能为空") @PathVariable String id) {
        return R.ok(actModelService.getInfo(id));
    }

    /**
     * 修改模型信息
     *
     * @param modelBo 模型数据
     */
    @Log(title = "模型管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping(value = "/update")
    public R<Void> update(@RequestBody ModelBo modelBo) {
        return toAjax(actModelService.update(modelBo));
    }

    /**
     * 编辑XMl模型
     *
     * @param modelBo 模型数据
     */
    @Log(title = "模型管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping(value = "/editModelXml")
    public R<Void> editModel(@Validated(EditGroup.class) @RequestBody ModelBo modelBo) {
        return toAjax(actModelService.editModelXml(modelBo));
    }

    /**
     * 删除流程模型
     *
     * @param ids 模型id
     */
    @Log(title = "模型管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{ids}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> delete(@NotEmpty(message = "主键不能为空") @PathVariable String[] ids) {
        Arrays.stream(ids).parallel().forEachOrdered(repositoryService::deleteModel);
        return R.ok();
    }

    /**
     * 模型部署
     *
     * @param id 模型id
     */
    @Log(title = "模型管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/modelDeploy/{id}")
    public R<Void> deploy(@NotBlank(message = "模型id不能为空") @PathVariable("id") String id) {
        return toAjax(actModelService.modelDeploy(id));
    }

    /**
     * 导出模型zip压缩包
     *
     * @param modelIds 模型id
     * @param response 相应
     */
    @GetMapping("/export/zip/{modelIds}")
    public void exportZip(@NotEmpty(message = "模型id不能为空") @PathVariable List<String> modelIds,
                          HttpServletResponse response) {
        actModelService.exportZip(modelIds, response);
    }

    /**
     * 复制模型
     *
     * @param modelBo 模型数据
     */
    @PostMapping("/copyModel")
    public R<Void> copyModel(@RequestBody ModelBo modelBo) {
        return toAjax(actModelService.copyModel(modelBo));
    }
}
