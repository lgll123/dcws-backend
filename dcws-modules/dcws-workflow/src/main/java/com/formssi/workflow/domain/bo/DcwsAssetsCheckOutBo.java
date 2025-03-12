package com.formssi.workflow.domain.bo;

import com.formssi.workflow.domain.DcwsAssetsCheckOut;
import com.formssi.workflow.domain.DcwsBaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsAssetsCheckOut.class, reverseConvertGenerate = false)
public class DcwsAssetsCheckOutBo extends DcwsBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 任务节点数据表Id
     */
    private String taskNodeDataId;

    /**
     * 物料类型 附属品:accessories、组件:components、许可证:licenses、消耗品:consumables、资产:hardware'
     */
    private String assetsType;

    /**
     * 使用人
     */
    private String checkOutUser;

    /**
     * 使用地点
     */
    private String checkOutLocal;
    /**
     * 物料信息
     */
    private String assetsDetail;

    /**
     * 错误码
     */
    private String code;

    /**
     * 失败原因
     */
    private String message;

    /**
     * 状态 0:失败 1:成功 2:部分成功
     */
    private String status;


    /**
     * auto处理次数
     */
    private int  autoHandleNum;

    /**
     * check类型，1-借出 2 借入
     */
    private String  checkType;
    /**
     * 资产系统附属品与用户关联Id
     */
    private String  accessoryUserId;
    /**
     * 1-流程checkout（借出）  2-流程checkinout(先归还再借出)
     */
    private String  checkOutIn;

}
