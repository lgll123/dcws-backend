package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.model.request.DmDictQueryRequest;
import com.formssi.generator.model.request.DmDictRequest;
import com.formssi.generator.model.vo.DmDictVO;
import com.formssi.generator.service.IDmDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据字典 操作处理
 *
 * @author Shen Tao
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/dm/dict")
public class DmDictController {

    private final IDmDictService dmDictService;

    @GetMapping("/page")
    public TableDataInfo<DmDictVO> selectPage(DmDictQueryRequest request, PageQuery pageQuery) {
        return dmDictService.selectPage(request, pageQuery);
    }

    @Log(title = "新增数据字典", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Long> addDict(@Validated @RequestBody DmDictRequest request) {
        Long dictId = dmDictService.addDict(request);
        return R.ok(dictId);
    }

    @Log(title = "修改数据字典", businessType = BusinessType.UPDATE)
    @PutMapping("/{dictId}")
    public R<Void> updateDict(@PathVariable("dictId") Long dictId, @Validated @RequestBody DmDictRequest request) {
        dmDictService.updateDict(dictId, request);
        return R.ok();
    }

    @Log(title = "修改数据字典", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{dictId}")
    public R<Void> deleteDict(@PathVariable("dictId") Long dictId) {
        dmDictService.deleteDict(dictId);
        return R.ok();
    }

}
