package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.generator.domain.TemplateGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.formssi.generator.service.ITemplateGroupService;

import java.util.List;
import java.util.Objects;

/**
 * @author : zsljava
 * @date Date : 2020-12-15 9:51
 * @Description: TODO
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/group")
public class TemplateGroupController {

    private final ITemplateGroupService templateGroupService;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @GetMapping("/list")
    public R<List<TemplateGroup>> listAll() {
        List<TemplateGroup> templateGroups = templateGroupService.listAll();
        return R.ok(templateGroups);
    }


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    @GetMapping("/get/{id}")
    public R<TemplateGroup> get(@PathVariable("id") int id) {
        TemplateGroup group = templateGroupService.getById(id);
        return R.ok(group);
    }

    /**
     * 新增，忽略null字段
     *
     * @param templateGroup 新增的记录
     * @return 返回影响行数
     */
    @PostMapping("/add")
    public R<TemplateGroup> insert(@RequestBody TemplateGroup templateGroup) {
        TemplateGroup group = templateGroupService.getByName(templateGroup.getGroupName());
        if (group != null) {
            throw new RuntimeException(templateGroup.getGroupName() + " 已存在");
        }
        templateGroupService.insertIgnoreNull(templateGroup);
        return R.ok(templateGroup);
    }

    /**
     * 修改，忽略null字段
     *
     * @param templateGroup 修改的记录
     * @return 返回影响行数
     */
    @PostMapping("/update")
    public R<Void> update(@RequestBody TemplateGroup templateGroup) {
        TemplateGroup group = templateGroupService.getByName(templateGroup.getGroupName());
        if (group != null && !Objects.equals(group.getId(), templateGroup.getId())) {
            throw new RuntimeException(templateGroup.getGroupName() + " 已存在");
        }
        templateGroupService.updateIgnoreNull(templateGroup);
        return R.ok();
    }

    /**
     * 删除记录
     *
     * @param templateGroup 待删除的记录
     * @return 返回影响行数
     */
    @PostMapping("/del")
    public R<Void> delete(@RequestBody TemplateGroup templateGroup) {
        templateGroupService.deleteGroup(templateGroup);
        return R.ok();
    }

}
