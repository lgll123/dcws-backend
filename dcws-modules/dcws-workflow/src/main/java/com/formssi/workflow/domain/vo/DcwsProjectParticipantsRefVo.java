package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsProjectParticipantsRef;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsProjectParticipantsRef.class)
public class DcwsProjectParticipantsRefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    @ExcelProperty(value = "项目id")
    private Long projectId;

    /**
     * 参与人ID
     */
    @ExcelProperty(value = "参与人ID")
    private Long participantsId;

    /**
     * 参与人姓名
     */
    @ExcelProperty(value = "参与人姓名")
    private String participantsName;

    /**
     * 参与人工号
     */
    @ExcelProperty(value = "参与人工号")
    private String participantsNo;

    /**
     * 参与类型
     */
    @ExcelProperty(value = "参与类型")
    private String participantsType;

}
