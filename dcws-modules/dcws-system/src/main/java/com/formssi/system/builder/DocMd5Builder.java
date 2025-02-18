package com.formssi.system.builder;

import com.formssi.system.domain.dto.DocInfoDTO;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/22 15:13
 */
public interface DocMd5Builder {

    /**
     * 生成文档md5
     *
     * @param docInfoDTO
     * @return
     */
    String buildMd5(DocInfoDTO docInfoDTO);

}
