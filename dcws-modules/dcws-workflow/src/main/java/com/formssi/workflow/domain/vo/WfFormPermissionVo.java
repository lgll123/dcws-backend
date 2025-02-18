package com.formssi.workflow.domain.vo;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表单权限
 * @author zhangmiao
 * @date 2025-01-20
 */
@Data
@AllArgsConstructor
public class WfFormPermissionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 表单Key
     */
    private boolean readable;

    /**
     * 表单名称
     */
    private boolean writable;


}
