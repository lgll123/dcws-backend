package com.formssi.system.builder;

import com.formssi.system.domain.dto.DocInfoDTO;
import com.formssi.system.domain.dto.DocParamDTO;
import com.formssi.system.enums.BooleanEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author thc
 */
public class DefaultDocMd5Builder implements DocMd5Builder {
    @Override
    public String buildMd5(DocInfoDTO docInfoDTO) {
        return getDocMd5(docInfoDTO);
    }

    public String getDocMd5(DocInfoDTO docInfoDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append(docInfoDTO.getName())
                .append(docInfoDTO.getDescription())
                .append(docInfoDTO.getAuthor())
                .append(docInfoDTO.getUrl())
                .append(docInfoDTO.getHttpMethod())
                .append(docInfoDTO.getParentId())
                .append(docInfoDTO.getModuleId())
                .append(docInfoDTO.getProjectId())
                .append(docInfoDTO.getIsUseGlobalHeaders())
                .append(docInfoDTO.getIsUseGlobalParams())
                .append(docInfoDTO.getIsUseGlobalReturns())
                .append(docInfoDTO.getIsRequestArray())
                .append(docInfoDTO.getIsResponseArray())
                .append(docInfoDTO.getRemark())
                .append(getDocParamsMd5(docInfoDTO));
        return DigestUtils.md5Hex(sb.toString());
    }

    public String getDocParamsMd5(DocInfoDTO docInfoDTO) {
        StringBuilder sb = new StringBuilder()
                .append(getParamsContent(docInfoDTO.getPathParams()))
                .append(getParamsContent(docInfoDTO.getHeaderParams()))
                .append(getParamsContent(docInfoDTO.getQueryParams()))
                .append(getParamsContent(docInfoDTO.getRequestParams()))
                .append(getParamsContent(docInfoDTO.getResponseParams()))
                .append(getParamsContent(docInfoDTO.getErrorCodeParams()));
        return DigestUtils.md5Hex(sb.toString());
    }

    public String getParamsContent(List<DocParamDTO> docParamDTOS) {
        if (CollectionUtils.isEmpty(docParamDTOS)) {
            return "";
        }
        return docParamDTOS.stream()
                .filter(docParamDTO -> Objects.equals(docParamDTO.getIsDeleted(), BooleanEnum.FALSE.getType()))
                .map(docParamDTO -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(docParamDTO.getName())
                            .append(docParamDTO.getType())
                            .append(docParamDTO.getRequired())
                            .append(docParamDTO.getExample())
                            .append(docParamDTO.getDescription())
                            .append(docParamDTO.getEnumId())
                            .append(docParamDTO.getIsDeleted())
                            .append(getParamsContent(docParamDTO.getChildren()));
                    return sb.toString();
                })
                .collect(Collectors.joining());
    }

}
