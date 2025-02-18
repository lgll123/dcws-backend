package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.generator.domain.DmCommonColumn;
import com.formssi.generator.model.vo.DmCommonGroupVO;
import com.formssi.generator.service.IDmCommonColumnService;
import com.formssi.generator.service.IDmCommonGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 常用字段分组 操作处理
 *
 * @author Shen Tao
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/dm/common")
public class DmCommonController {

    private final IDmCommonGroupService genCommonGroupService;

    private final IDmCommonColumnService genCommonColumnService;

    /**
     * 查询所有常用字段分组
     */
    @GetMapping("/group/list")
    public R<List<DmCommonGroupVO>> genList() {
        return R.ok(genCommonGroupService.listAll());
    }

    /**
     * 查询指定分组的所有常用字段
     */
    @GetMapping("/column/group/{groupId}")
    public R<List<DmCommonColumn>> columnListByCommonGroupId(@PathVariable("groupId") Long commonGroupId) {
        return R.ok(genCommonColumnService.listByCommonGroupId(commonGroupId));
    }

}
