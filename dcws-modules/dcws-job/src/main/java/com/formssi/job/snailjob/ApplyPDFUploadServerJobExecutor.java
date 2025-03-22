package com.formssi.job.snailjob;

import cn.hutool.core.util.ObjectUtil;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.minio.util.MinioUtil;
import com.formssi.workflow.common.enums.ApplyTypeEnum;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.service.UploadFileServerService;
import com.formssi.workflow.service.IApplyService;
import com.formssi.workflow.service.strategy.DcwsApplyFilePDFCreateStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.formssi.workflow.common.enums.ApplyTypeEnum.of;
import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

/**
 * @author opensnail
 * @date 2024-05-17
 * 不同的申请类型生成PDF需要在枚举ApplyTypeEnum中添加，并新增对应的处理类如DcwsMaterialApplyFilePDFCreateStrategy，
 * ApplyTypeEnum枚举中的name 对应处理类DcwsMaterialApplyFilePDFCreateStrategy bean name，eg:material
 */
@Slf4j
@Component
@JobExecutor(name = "applyPDFUploadServerJobExecutor")
public class ApplyPDFUploadServerJobExecutor {
    @Autowired
    private IApplyService applyService;
    @Autowired
    private UploadFileServerService uploadFileServerService;
    @Autowired
    private MinioUtil minioUtil;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("applyPDFUploadServerJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        SnailJobLog.REMOTE.info("applyPDFUploadServerJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        //查询已完成finish待生成PDF的申请列表，分页(默认10条)
        Page<TaskNodeDataVo> taskNodeDataVoPage = applyService.queryPageApplyPDF();
        List<TaskNodeDataVo> records = taskNodeDataVoPage.getRecords();
        if(ObjectUtil.isEmpty(records)) {
            return ExecuteResult.success("查询列表为null");
        }
        for (int i = 0; i < records.size(); i++) {
            TaskNodeDataVo taskNodeDataVo = records.get(i);
            try{
                ApplyTypeEnum applyTypeEnum = of(taskNodeDataVo.getApplyType());
                if(applyTypeEnum == null){
                    log.error("申请类型applyType:{} 不存在或未添加枚举值",taskNodeDataVo.getApplyType());
                    continue;
                }
                DcwsApplyFilePDFCreateStrategy dcwsApplyFilePDFCreateStrategy = SpringUtils.getBean(applyTypeEnum.getName());

                // 转成PDF
                Map<String, Object> pdfResultMap = dcwsApplyFilePDFCreateStrategy.process(applyTypeEnum.getName(), taskNodeDataVo);
                byte[] pdfBytes = (byte[])pdfResultMap.get(PDFBYTES);
                String fileName = (String)pdfResultMap.get(FILENAME);
                Map<String, Object> documentServerParam = (Map<String, Object>)pdfResultMap.get(DOCUMENTSERVERPARAM);
                // 上传PDF到minio/document server
                uploadFileServerService.uploadFileMinioAndDocumentServer(pdfBytes,fileName,documentServerParam,taskNodeDataVo);
//                SnailJobLog.LOCAL.info("taskNodeDataMapper.updateById result:{}", ret);
            } catch (Exception e) {
                log.error("PDF生成失败{}", e.getMessage(), e);
                SnailJobLog.LOCAL.info("applyPDFUploadServerJobExecutor. exception:{}", e.getMessage());
                // 更新文件上传状态
                TaskNodeDataBo bo = new TaskNodeDataBo();
                bo.setStorageFileStatus(0);
                bo.setId(taskNodeDataVo.getId());
                applyService.updateByBo(bo);
                SnailJobLog.LOCAL.info("taskNodeDataMapper.updateById result:{}");
            }
        }
        return ExecuteResult.success("PDF生成任务执行完成");
    }
}
