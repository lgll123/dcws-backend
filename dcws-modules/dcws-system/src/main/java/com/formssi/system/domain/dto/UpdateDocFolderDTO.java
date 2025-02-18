package com.formssi.system.domain.dto;

import com.formssi.common.core.domain.model.LoginUser;
import lombok.Data;

/**
 * @author lizhangyu
 */
@Data
public class UpdateDocFolderDTO {

    private Long id;
    private String name;
    private Long parentId;
    private LoginUser loginUser;

}
