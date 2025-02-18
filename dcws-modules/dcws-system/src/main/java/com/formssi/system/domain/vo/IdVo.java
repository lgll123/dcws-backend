package com.formssi.system.domain.vo;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class IdVo {

    private Long id;

    public IdVo(Long id) {
        this.id = id;
    }
}
