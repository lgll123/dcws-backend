package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.generator.domain.TypeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.formssi.generator.service.ITypeConfigService;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/type")
public class TypeConfigController {

    private final ITypeConfigService typeConfigService;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @GetMapping("/list")
    public R<List<TypeConfig>> listAll() {
        return R.ok(typeConfigService.listAll());
    }


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    @GetMapping("/getById")
    public TypeConfig getById(Integer id) {
        return typeConfigService.getById(id);
    }

    /**
     * 修改，忽略null字段
     *
     * @param typeConfigList 修改的记录
     * @return 返回影响行数
     */
    @PostMapping("/update")
    public R<Void> update(@RequestBody List<TypeConfig> typeConfigList) {
        for (TypeConfig typeConfig : typeConfigList) {
            typeConfigService.updateIgnoreNull(typeConfig);
        }
        return R.ok();
    }

}
