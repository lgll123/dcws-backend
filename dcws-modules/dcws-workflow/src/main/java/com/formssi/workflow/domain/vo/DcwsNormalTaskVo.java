package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsNormalTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsNormalTask.class)
public class DcwsNormalTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ExcelProperty(value = "任务ID")
    private String taskId;

    /**
     * 项目id
     */
    @ExcelProperty(value = "项目id")
    private Long projectId;

    /**
     * 父级任务id
     */
    @ExcelProperty(value = "父级任务id")
    private String parentTaskId;

    /**
     * 业务id
     */
    @ExcelProperty(value = "业务id")
    private String businessKey;

    /**
     * 任务名称
     */
    @ExcelProperty(value = "任务名称")
    private String taskName;

    /**
     * 处理说明
     */
    @ExcelProperty(value = "处理说明")
    private String remark;

    /**
     * 办理人id
     */
    @ExcelProperty(value = "办理人id")
    private String userId;

    /**
     * 处理状态
     */
    @ExcelProperty(value = "处理状态")
    private String status;

    /**
     * 文件ID
     */
    @ExcelProperty(value = "文件ID")
    private String fileId;

    /**
     * 开始时间
     */
    @ExcelProperty(value = "开始时间")
    private Date createTime;

    /**
     * 结束时间
     */
    @ExcelProperty(value = "结束时间")
    private Date updateTime;

    /**
     * 办理人集合
     */
    private List<DcwsNormalTaskUserVo> dcwsUserVoList;

    /**
     * 公司
     */
    private String companyId;


}
