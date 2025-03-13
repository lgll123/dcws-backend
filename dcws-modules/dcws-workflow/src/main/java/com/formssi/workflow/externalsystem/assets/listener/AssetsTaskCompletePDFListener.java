package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.minio.util.MinioUtil;
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
import java.util.stream.Collectors;

/**
 * 采购任务启动
 */
@Slf4j
@Component("AssetsTaskCompletePDFListener")
public class AssetsTaskCompletePDFListener implements ExecutionListener {
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private IApplyService applyService;
    @Autowired
    private UserService userService;

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
            data.put("status","已完成");//当前环节 TODO
            String requiredDateType = taskNodeDataVo.getRequiredDateType();
            String requiredDate = switch (StringUtils.blankToDefault(requiredDateType,"4")) {
                case "1" -> "截止日期  " + DateUtil.format(taskNodeDataVo.getCompleteDate(), "yyyy-MM-dd");
                case "2" -> taskNodeDataVo.getRequiredDesc();
                case "3" -> "尽快";
                default -> null;
            };
            data.put("requiredDateType",requiredDate);//需求日期
            data.put("checkTo",taskNodeDataVo.getCheckTo());//预计使用人
            data.put("applyReson",taskNodeDataVo.getApplyReson());//申请原因
            data.put("applyRemarks",taskNodeDataVo.getApplyRemarks());//备注
            data.put("approver","管理员");//审批人 TODO

            String applyDetail = taskNodeDataVo.getApplyDetail();
            Map<String,Object> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(applyDetail), Map.class);
            //附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
            ArrayList<Map<String, Object>> hardware = (ArrayList<Map<String, Object>>) applyDetails.get("hardware");
            ArrayList<Map<String, Object>> licenses = (ArrayList<Map<String, Object>>) applyDetails.get("licenses");
            ArrayList<Map<String, Object>> accessories = (ArrayList<Map<String, Object>>) applyDetails.get("accessories");
            ArrayList<Map<String, Object>> components = (ArrayList<Map<String, Object>>) applyDetails.get("components");
            ArrayList<Map<String, Object>> consumables = (ArrayList<Map<String, Object>>) applyDetails.get("consumables");
            Map<String, Object> purchaseDetail = (Map<String, Object>) applyDetails.get("purchaseDetail");
            String purchase = Convert.toStr(applyDetails.get("purchase"));
            String purchaseTrans = switch (purchase) {
                case "0" -> "有库存";
                case "1" -> "需采购";
                case "2" -> "部分需采购";
                default -> null;
            };
            data.put("purchase",purchaseTrans);//2:部分采购
            data.put("hardware", hardware);
            data.put("licenses", licenses);
            data.put("accessories", accessories);
            data.put("components", components);
            data.put("consumables", consumables);
            data.put("purchaseDetail", purchaseDetail);
            String customApplyDetail1 = taskNodeDataVo.getCustomApplyDetail();
            List<Map<String,Object>> customApplyDetails = objectMapper.readValue(JSONUtil.toJsonStr(customApplyDetail1), List.class);
            data.put("customApplyDetails", customApplyDetails);
            // 审批记录
            List<ActHistoryInfoVo> historyRecords = actProcessInstanceService.getHistoryRecord(taskNodeDataVo.getId());
            // 查询审批人昵称名称
            List<ActHistoryInfoVo> collect = historyRecords.stream()
                    .map(h -> {
                        if(!StringUtils.isEmpty(h.getAssignee())){
                            h.setNickName(userService.selectNicknameById(Convert.toLong(h.getAssignee())));
                        }else {
                            h.setNickName("无");
                        }
                        if(StringUtils.isEmpty(h.getComment())){
                            h.setComment("无");
                        }
                        if(StringUtils.isEmpty(h.getStatusName())){
                            h.setStatusName("通过");
                        }
                        return h;
                    }).toList();
            data.put("historyRecords", collect);

            // 生成PDF
            byte[] pdfBytes = pdfGeneratorService.generatePdf("material", data);
            // 转换为 InputStream
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);
            String documentTypeId=null;
            String storagePathId=null;
            String[] tags=null;
            String objName=null;
            String applyType = taskNodeDataVo.getApplyType();//19 IT 21 非IT
            if("19".equals(applyType)){
                documentTypeId="3";
                storagePathId="2";
                tags=new String[]{"6"};
                objName="IT物料申请-";
            }
            if("21".equals(applyType)){
                documentTypeId="3";
                storagePathId="3";
                tags=new String[]{"7"};
                objName="非IT物料申请-";
            }


            // 上传到文件服务器
            minioUtil.createBucket("dcws-assets");
            minioUtil.uploadFile(inputStream, "dcws-assets", objName+taskNodeDataVo.getId()+".pdf");
            // 获取永久访问URL
            String fileUrl = minioUtil.getPermanentTimePreviewUrl("dcws-assets", objName+taskNodeDataVo.getId()+".pdf");
            log.info("获取永久访问URL: "+fileUrl);
            //上传文件到档案系统
            String taskId = pdfGeneratorService.uploadDocument(pdfBytes, objName + taskNodeDataVo.getId() + ".pdf",
                    taskNodeDataVo.getId(), null, null, documentTypeId, storagePathId, tags,
                    null, null);

             // 插入数据
            SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
            sysFileUploadVo.setUrl(fileUrl);
            sysFileUploadVo.setFileName(objName+taskNodeDataVo.getId()+".pdf");
            pdfGeneratorService.insertUploadResult(sysFileUploadVo, objName+taskNodeDataVo.getId()+".pdf");
            // 插入文件上传服务器记录存储表数据
            DcwsSysFileVo dcwsSysFileVo = new DcwsSysFileVo();
            dcwsSysFileVo.setFileUrl(fileUrl);
            dcwsSysFileVo.setTaskNodeDataId(taskNodeDataVo.getId());
            dcwsSysFileVo.setFileName(objName+taskNodeDataVo.getId()+".pdf");
            pdfGeneratorService.insertUploadRecord(dcwsSysFileVo, objName+taskNodeDataVo.getId()+".pdf");
        } catch (Exception e) {
            log.error("An error occurred while AssetsApplyTaskExeListener", e);
        }
    }




}
