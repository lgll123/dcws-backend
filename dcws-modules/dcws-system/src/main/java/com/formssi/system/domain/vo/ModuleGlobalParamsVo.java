package com.formssi.system.domain.vo;

import com.formssi.system.domain.dto.DocParamDTO;
import lombok.Data;

import java.util.List;

/**
 * @author thc
 */
@Data
public class ModuleGlobalParamsVo {
    private List<DocParamDTO> globalHeaders;
    private List<DocParamDTO> globalParams;
    private List<DocParamDTO> globalReturns;
}
