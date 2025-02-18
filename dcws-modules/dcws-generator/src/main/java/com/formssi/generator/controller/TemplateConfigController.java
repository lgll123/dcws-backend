package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.generator.domain.TemplateConfig;
import com.formssi.generator.domain.TemplateGroup;
import com.formssi.generator.util.DecodeURIUtil;
import com.formssi.generator.util.TemplateMetaUtils;
import com.formssi.generator.service.ITemplateConfigService;
import com.formssi.generator.service.ITemplateGroupService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/template")
public class TemplateConfigController {

    private final ITemplateConfigService templateConfigService;

    private final ITemplateGroupService templateGroupService;

    @PostMapping("/add")
    public R<TemplateConfig> add(@RequestBody TemplateConfig templateConfig) {
        templateConfigService.insert(templateConfig);
        return R.ok(templateConfig);
    }

    @GetMapping("/get/{id}")
    public R<TemplateConfig> get(@PathVariable("id") int id) {
        return R.ok(templateConfigService.getById(id));
    }

    @GetMapping("/list")
    public R<List<TemplateConfig>> list(@RequestParam(required = false) String groupId) {
        List<TemplateConfig> templateConfigs = null;
        if(StringUtils.isEmpty(groupId)){
            templateConfigs = templateConfigService.listAll();
        }else {
            templateConfigs = templateConfigService.listByGroupId(groupId);
        }
        Map<Integer, String> idMap = templateGroupService.listAll()
                .stream()
                .collect(Collectors.toMap(TemplateGroup::getId, TemplateGroup::getGroupName));
        for (TemplateConfig templateConfig : templateConfigs) {
            Integer gid = templateConfig.getGroupId();
            if (gid != null) {
                String groupName = idMap.getOrDefault(gid, "");
                templateConfig.setGroupName(groupName);
            }
            templateConfig.setContent(TemplateMetaUtils.generateMetaContent(templateConfig));
        }
        return R.ok(templateConfigs);
    }

    @PostMapping("/update")
    public R<Void> update(@RequestBody TemplateConfig templateConfig) {
        String content = DecodeURIUtil.decodeURIComponent(templateConfig.getContent());
        templateConfig.setContent(content);
        templateConfigService.update(templateConfig);
        return R.ok();
    }

    @PostMapping("/del")
    public R<Void> del(@RequestBody TemplateConfig templateConfig) {
        templateConfigService.delete(templateConfig);
        return R.ok();
    }

    @PutMapping("/save")
    public R<Void> save(@RequestBody TemplateConfig templateConfig) {
        String content = DecodeURIUtil.decodeURIComponent(templateConfig.getContent());
        templateConfig.setContent(content);
        templateConfigService.save(templateConfig);
        return R.ok();
    }

    @PostMapping("/copy")
    public R<Void> copy(@RequestBody TemplateConfig templateConfig) {
        templateConfigService.copy(templateConfig);
        return R.ok();
    }

}
