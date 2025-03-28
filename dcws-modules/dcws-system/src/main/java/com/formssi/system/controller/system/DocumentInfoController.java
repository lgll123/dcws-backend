package com.formssi.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.formssi.common.core.domain.R;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.system.domain.bo.DocumentInfoBo;
import com.formssi.system.domain.vo.DocumentInfoVo;
import com.formssi.system.service.IDocumentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资料-部门维护信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/document")
public class DocumentInfoController extends BaseController {

    private final IDocumentInfoService documentInfoService;

    /**
     * 获取资料-部门列表
     */
    @SaCheckPermission("system:document:list")
    @GetMapping("/list")
    public R<List<DocumentInfoVo>> list(DocumentInfoBo info) {
        List<DocumentInfoVo> infos = documentInfoService.selectDocumentInfoList(info);
        return R.ok(infos);
    }

    /**
     * 获取资料-部门列表-分页
     */
    @SaCheckPermission("system:document:listByPage")
    @GetMapping("/listByPage")
    public TableDataInfo<DocumentInfoVo> list(DocumentInfoBo info, PageQuery pageQuery) {
        return documentInfoService.selectPageDocumentList(info, pageQuery);
    }

    /**
     * 新增资料-部门
     */
    @SaCheckPermission("system:document:add")
    @Log(title = "资料-部门", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<Void> add(@Validated @RequestBody DocumentInfoBo info) {
        if (!documentInfoService.checkSealNameUnique(info)) {
            return R.fail("新增资料-部门" + info.getDeptName() + "'失败，部门名称已存在");
        }
        if (!documentInfoService.checkIsDeaultDept(info)) {
            return R.fail("新增资料-部门" + info.getDeptName() + "'失败，只能有一个默认部门（发展部）");
        }
        documentInfoService.insertDocument(info);
        return R.ok();
    }

    /**
     * 修改资料-部门
     */
    @SaCheckPermission("system:document:edit")
    @Log(title = "资料-部门", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public R<Void> edit(@Validated @RequestBody DocumentInfoBo info) {
        if (!documentInfoService.checkSealNameUnique(info)) {
            return R.fail("修改资料-部门" + info.getDeptName() + "'失败，部门名称已存在");
        }
        if (!documentInfoService.checkIsDeaultDept(info)) {
            return R.fail("新增资料-部门" + info.getDeptName() + "'失败，只能有一个默认部门（发展部）");
        }
        documentInfoService.updateDocument(info);
        return R.ok();
    }

    /**
     * 删除资料-部门
     */
    @SaCheckPermission("system:document:delete")
    @Log(title = "资料-部门", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody DocumentInfoBo info) {
        documentInfoService.deleteDocumentById(info.getId());
        return R.ok();
    }

}
