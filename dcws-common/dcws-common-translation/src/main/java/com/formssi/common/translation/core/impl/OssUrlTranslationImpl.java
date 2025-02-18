package com.formssi.common.translation.core.impl;

import com.formssi.common.translation.annotation.TranslationType;
import com.formssi.common.translation.constant.TransConstant;
import com.formssi.common.translation.core.TranslationInterface;
import com.formssi.common.core.service.OssService;
import lombok.AllArgsConstructor;

/**
 * OSS翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.OSS_ID_TO_URL)
public class OssUrlTranslationImpl implements TranslationInterface<String> {

    private final OssService ossService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof String ids) {
            return ossService.selectUrlByIds(ids);
        } else if (key instanceof Long id) {
            return ossService.selectUrlByIds(id.toString());
        }
        return null;
    }
}
