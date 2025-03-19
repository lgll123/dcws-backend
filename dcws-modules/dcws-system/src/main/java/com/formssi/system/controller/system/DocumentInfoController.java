package com.formssi.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.formssi.common.core.domain.R;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.system.domain.bo.DocumentInfoBo;
import com.formssi.system.domain.bo.SealInfoBo;
import com.formssi.system.domain.vo.DocumentInfoVo;
import com.formssi.system.domain.vo.SealInfoVo;
import com.formssi.system.service.IDocumentInfoService;
import com.formssi.system.service.ISealInfoService;
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
     * 获取印章列表
     */
    @SaCheckPermission("system:document:list")
    @GetMapping("/list")
    public R<List<DocumentInfoVo>> list(DocumentInfoBo info) {
        List<DocumentInfoVo> infos = documentInfoService.selectDocumentInfoList(info);
        return R.ok(infos);
    }

//    /**
//     * 获取印章列表-分页
//     */
//    @SaCheckPermission("system:seal:listByPage")
//    @GetMapping("/listByPage")
//    public TableDataInfo<SealInfoVo> list(SealInfoBo info, PageQuery pageQuery) {
//        return sealInfoService.selectPageUserList(info, pageQuery);
//    }
//
//    /**
//     * 新增印章
//     */
//    @SaCheckPermission("system:seal:add")
//    @Log(title = "印章", businessType = BusinessType.INSERT)
//    @PostMapping("/add")
//    public R<Void> add(@Validated @RequestBody SealInfoBo info) {
//        if (!sealInfoService.checkSealNameUnique(info)) {
//            return R.fail("新增印章" + info.getSealName() + "'失败，印章名称已存在");
//        }
//        sealInfoService.insertSeal(info);
//        return R.ok();
//    }
//
//    /**
//     * 修改印章
//     */
//    @SaCheckPermission("system:seal:edit")
//    @Log(title = "印章", businessType = BusinessType.UPDATE)
//    @PostMapping("/edit")
//    public R<Void> edit(@Validated @RequestBody SealInfoBo info) {
//        if (!sealInfoService.checkSealNameUnique(info)) {
//            return R.fail("修改印章" + info.getSealName() + "'失败，印章名称已存在");
//        }
//        sealInfoService.updateSeal(info);
//        return R.ok();
//    }
//
//    /**
//     * 删除印章
//     */
//    @SaCheckPermission("system:seal:delete")
//    @Log(title = "印章", businessType = BusinessType.DELETE)
//    @PostMapping("/delete")
//    public R<Void> delete(@Validated @RequestBody SealInfoBo info) {
//        sealInfoService.deleteSealById(info.getId());
//        return R.ok();
//    }

}
