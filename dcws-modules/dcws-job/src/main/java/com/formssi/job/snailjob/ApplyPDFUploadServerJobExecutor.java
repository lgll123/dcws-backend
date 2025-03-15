package com.formssi.job.snailjob;

import cn.hutool.core.util.ObjectUtil;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.minio.util.MinioUtil;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.workflow.common.enums.ApplyTypeEnum;
import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.TaskNodeData;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.service.UploadFileServerService;
import com.formssi.workflow.mapper.TaskNodeDataMapper;
import com.formssi.workflow.service.strategy.DcwsApplyFilePDFCreateStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.formssi.workflow.common.enums.ApplyTypeEnum.*;


/**
 * @author opensnail
 * @date 2024-05-17
 */
@Slf4j
@Component
@JobExecutor(name = "applyPDFUploadServerJobExecutor")
public class ApplyPDFUploadServerJobExecutor {
    @Autowired
    private TaskNodeDataMapper taskNodeDataMapper;

    @Autowired
    private UploadFileServerService uploadFileServerService;
    @Autowired
    private MinioUtil minioUtil;
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("applyPDFUploadServerJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        SnailJobLog.REMOTE.info("applyPDFUploadServerJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
        lqw.eq(TaskNodeData::getStatus, "finish");
        lqw.eq(TaskNodeData::getStorageFileStatus, "4");
        lqw.in(TaskNodeData::getApplyType, Arrays.asList(MATERIAL_IT.getCode(),MATERIAL_NOT_IT.getCode()));
        lqw.orderByDesc(DcwsBaseEntity::getCreateTime);
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);
        Page<TaskNodeDataVo> result = taskNodeDataMapper.selectVoPage(pageQuery.build(), lqw);
        List<TaskNodeDataVo> records = result.getRecords();
        if(ObjectUtil.isEmpty(records)) {
            return ExecuteResult.success("查询列表为null");
        }
        for (int i = 0; i < records.size(); i++) {
            TaskNodeDataVo taskNodeDataVo = records.get(i);
            try{
                DcwsApplyFilePDFCreateStrategy dcwsApplyFilePDFCreateStrategy;
                switch (ApplyTypeEnum.of(taskNodeDataVo.getApplyType())){
                    case MATERIAL_IT : //IT物料申请
                        dcwsApplyFilePDFCreateStrategy = SpringUtils.getBean(MATERIAL_IT.getName());
                        break;
                    case MATERIAL_NOT_IT://非IT物料申请
                        dcwsApplyFilePDFCreateStrategy = SpringUtils.getBean(MATERIAL_NOT_IT.getName());
                        break;
                    case SEAL://用印申请
                        dcwsApplyFilePDFCreateStrategy = SpringUtils.getBean(SEAL.getName());
                        break;
                    default: dcwsApplyFilePDFCreateStrategy = null;
                }
                // 转成PDF
                Map<String, Object> pdfResultMap = dcwsApplyFilePDFCreateStrategy.process(ApplyTypeEnum.of(taskNodeDataVo.getApplyType()).getName(), taskNodeDataVo);
                byte[] pdfBytes = (byte[])pdfResultMap.get("pdfBytes");
                String fileName = (String)pdfResultMap.get("fileName");
                Map<String, Object> documentServerParam = (Map<String, Object>)pdfResultMap.get("documentServerParam");
                // 上传PDF到minio/document server
                uploadFileServerService.uploadFileMinioAndDocumentServer(pdfBytes,fileName,documentServerParam,taskNodeDataVo);
//                SnailJobLog.LOCAL.info("taskNodeDataMapper.updateById result:{}", ret);
            } catch (Exception e) {
                log.error("PDF生成失败{}", e.getMessage(), e);
                SnailJobLog.LOCAL.info("applyPDFUploadServerJobExecutor. exception:{}", e.getMessage());
                // 更新文件上传状态
                TaskNodeData convert = MapstructUtils.convert(taskNodeDataVo, TaskNodeData.class);
                convert.setStorageFileStatus(0);
                int re = taskNodeDataMapper.updateById(convert);
                SnailJobLog.LOCAL.info("taskNodeDataMapper.updateById result:{}", re);
            }
        }
        return ExecuteResult.success("PDF生成成功");
    }
}
