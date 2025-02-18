package com.formssi.generator.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.domain.R;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.generator.domain.DmLayout;
import com.formssi.generator.model.request.DmLayoutPageRequest;
import com.formssi.generator.model.request.DmLayoutTableRequest;
import com.formssi.generator.service.IDmLayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 设计服务 操作处理
 *
 * @author Shen Tao
 */
@Validated
@RequiredArgsConstructor
@RestController
public class DmLayoutController extends BaseController {

    private final IDmLayoutService genLayoutService;

    @GetMapping("/dm/layouts")
    public TableDataInfo<DmLayout> getLayoutPage(DmLayoutPageRequest layoutPageRequest, PageQuery pageQuery) {
        return genLayoutService.findLayoutPage(layoutPageRequest, pageQuery);
    }

    @GetMapping("/dm/layout/{layoutId}")
    public R<Object> getLayout(@PathVariable("layoutId") Long layoutId) {
        DmLayout layout = genLayoutService.getLayout(layoutId);
        if (layout == null) {
            return R.fail("该ER图不存在");
        }
        return R.ok("SUCCESS", JSON.parse(layout.getLayoutData()));
    }

    @Log(title = "新增设计信息", businessType = BusinessType.UPDATE)
    @PostMapping("/dm/layout")
    public R<Void> addLayout(@Validated @RequestBody String requestBody) {
        genLayoutService.addLayout(requestBody);
        return R.ok();
    }

    @Log(title = "保存设计信息", businessType = BusinessType.UPDATE)
    @PutMapping("/dm/layout/{layoutId}")
    public R<Void> updateLayout(@PathVariable("layoutId") Long layoutId, @Validated @RequestBody String requestBody) {
        genLayoutService.updateLayout(layoutId, requestBody);
        return R.ok();
    }

    @Log(title = "删除设计信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/dm/layout/{layoutIds}")
    public R<Void> deleteLayout(@PathVariable Long[] layoutIds) {
        genLayoutService.deleteLayoutByIds(layoutIds);
        return R.ok();
    }

//    @GetMapping("/dm/layout/table/{tableId}")
//    public R<DmTableVO> getTableLayout(@PathVariable("tableId") Long tableId) {
//        DmTableVO tableVO = genLayoutService.getTableLayout(tableId);
//        return R.ok(tableVO);
//    }

    @Log(title = "应用表的设计", businessType = BusinessType.UPDATE)
    @PostMapping("/dm/layout/table/apply")
    public R<Void> applyTableLayout(@Validated @RequestBody DmLayoutTableRequest layoutTableRequest) {
        genLayoutService.applyTableLayout(layoutTableRequest, false);
        return R.ok();
    }

    @Log(title = "强制应用表的设计", businessType = BusinessType.UPDATE)
    @PostMapping("/dm/layout/table/apply/forced")
    public R<Void> applyTableLayoutForced(@Validated @RequestBody DmLayoutTableRequest layoutTableRequest) {
        genLayoutService.applyTableLayout(layoutTableRequest, true);
        return R.ok();
    }

}
