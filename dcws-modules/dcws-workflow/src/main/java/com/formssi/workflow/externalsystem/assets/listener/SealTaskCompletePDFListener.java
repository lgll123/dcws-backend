package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.minio.util.MinioUtil;
import com.formssi.system.domain.vo.SealJsonVo;
import com.formssi.system.domain.vo.SysFileUploadVo;
import com.formssi.system.service.ISysDeptService;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.vo.ActHistoryInfoVo;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.service.PdfGeneratorService;
import com.formssi.workflow.service.IActProcessInstanceService;
import com.formssi.workflow.service.IApplyService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用印申请生成pdf
 */
@Slf4j
@Component("SealTaskCompletePDFListener")
public class SealTaskCompletePDFListener implements ExecutionListener {
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private IApplyService applyService;
    @Autowired
    private IActProcessInstanceService actProcessInstanceService;
    @Autowired
    private PdfGeneratorService pdfGeneratorService;
    @Autowired
    private MinioUtil minioUtil;
    @Override
    public void notify(DelegateExecution delegateTask) {
        try{
            Map<String, Object> variables = delegateTask.getVariables();
            Object entity = variables.get("entity");
            if (ObjectUtil.isEmpty(entity)) return;
            ObjectMapper objectMapper = new ObjectMapper();
            TaskNodeDataBo taskNodeDataBo = objectMapper.readValue(JSONUtil.toJsonStr(entity), TaskNodeDataBo.class);
            TaskNodeDataVo taskNodeDataVo = applyService.queryById(taskNodeDataBo.getId());//TODO
            Map<String, Object> data = new HashMap<>();

            data.put("id",taskNodeDataVo.getId());//申请编号
            data.put("applyDept",taskNodeDataVo.getApplyDept());//申请部门
            data.put("applicant",taskNodeDataVo.getApplicant());//申请人
            data.put("applyDate",taskNodeDataVo.getApplyDate());//申请日期
            data.put("completeDate",taskNodeDataVo.getCompleteDate());//使用日期
            data.put("status",taskNodeDataVo.getStatus());//当前环节
            data.put("applyReson",taskNodeDataVo.getApplyReson());//申请原因
            data.put("applyRemarks",taskNodeDataVo.getApplyRemarks());//备注
            data.put("approver","管理员");//审批人 TODO

            String applyDetail = taskNodeDataVo.getApplyDetail();
            List<Map<String,Object>> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(applyDetail), List.class);

            data.put("applyDetails", applyDetails);

            // 审批记录
            List<ActHistoryInfoVo> historyRecords = actProcessInstanceService.getHistoryRecord(taskNodeDataVo.getId());
            data.put("historyRecords", historyRecords);
            // 生成PDF
            byte[] pdfBytes = pdfGeneratorService.generatePdf("seal", data);
            // 转换为 InputStream
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);
            // 上传到文件服务器
            minioUtil.createBucket("dcws-seal");
            minioUtil.uploadFile(inputStream, "dcws-seal", "用印申请-"+taskNodeDataVo.getId()+".pdf");
            // 获取永久访问URL
            String fileUrl = minioUtil.getPermanentTimePreviewUrl("dcws-seal", "用印申请-"+taskNodeDataVo.getId()+".pdf");
            log.info("获取永久访问URL: "+fileUrl);
            //上传文件到档案系统
            String taskId = pdfGeneratorService.uploadDocument(pdfBytes, "用印申请-" + taskNodeDataVo.getId() + ".pdf",
                    taskNodeDataVo.getId(), null, null, "3", "4",
                    new String[]{"8"}, null, null);

             // 插入数据
            SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
            sysFileUploadVo.setUrl(fileUrl);
            sysFileUploadVo.setFileName("用印申请-"+taskNodeDataVo.getId()+".pdf");
            pdfGeneratorService.insertUploadResult(sysFileUploadVo, "用印申请-"+taskNodeDataVo.getId()+".pdf");
            // 插入文件上传服务器记录存储表数据
            DcwsSysFileVo dcwsSysFileVo = new DcwsSysFileVo();
            dcwsSysFileVo.setFileUrl(fileUrl);
            dcwsSysFileVo.setFileName("用印申请-"+taskNodeDataVo.getId()+".pdf");
            pdfGeneratorService.insertUploadRecord(dcwsSysFileVo, "用印申请-"+taskNodeDataVo.getId()+".pdf");
        } catch (Exception e) {
            log.error("An error occurred while AssetsApplyTaskExeListener", e);
        }
    }




}
