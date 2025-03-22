package com.formssi.workflow.service.strategy;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.common.enums.ApplyContentTypeEnum;
import com.formssi.workflow.domain.vo.DcwsActHistoryInfoVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.service.DcwsIActProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.formssi.workflow.common.enums.ApplyContentTypeEnum.*;
import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

/**
 * dcws 服务申请申请单生成PDF数据处理接口策略
 *
 * @author yqh
 */
@Slf4j
@Service("service")
@RequiredArgsConstructor
public class DcwsServiceApplyFilePDFCreateStrategy implements DcwsApplyFilePDFCreateStrategy<TaskNodeDataVo> {
    @Autowired
    private UserService userService;
    @Autowired
    private DcwsIActProcessInstanceService dcwsIActProcessInstanceService;
    @Autowired
    private CreateApplyFilePDFService createApplyFilePDFService;


    @Override
    public Map<String, Object> process(String templateName,TaskNodeDataVo taskNodeDataVo) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();
            data.put("id",taskNodeDataVo.getId());//申请编号
            data.put("applyDept",taskNodeDataVo.getApplyDept());//申请部门
            data.put("applicant",taskNodeDataVo.getApplicant());//申请人
            data.put("applyDate",taskNodeDataVo.getApplyDate());//申请日期
            String applyContentTypeDesc;
            switch (ApplyContentTypeEnum.of(taskNodeDataVo.getApplyContentType())){
                case SERVICE_1: applyContentTypeDesc = SERVICE_1.getDesc();
                    break;
                case SERVICE_2: applyContentTypeDesc = SERVICE_2.getDesc();
                    break;
                case SERVICE_3: applyContentTypeDesc = SERVICE_3.getDesc();
                    break;
                default: applyContentTypeDesc = taskNodeDataVo.getApplyContentType();
            }
            data.put("applyContentType",applyContentTypeDesc);//类型
            data.put("checkTo",taskNodeDataVo.getCheckTo());//预计使用人
            data.put("applyReson",taskNodeDataVo.getApplyReson());//申请原因
            data.put("applyRemarks",taskNodeDataVo.getApplyRemarks());//备注

            String applyDetail = taskNodeDataVo.getApplyDetail();
            Map<String,Object> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(applyDetail), Map.class);
            //ITDept
            Map<String, Object> ITDept = (Map<String, Object>) applyDetails.get("ITDept");
            data.put("purchase",ITDept.get("purchase"));//1:采购
            data.put("projectName",ITDept.get("projectName"));//1:采购项目名称
            data.put("purchaseDetail", ITDept.get("purchaseDetail"));
            data.put("hardware", ITDept.get("hardware"));//资产信息
            Map<String, Object> extMaint = (Map<String, Object>) ITDept.get("extMaint");
            List<Map<String, Object>> processMethod = ObjectUtil.isEmpty(extMaint)?null:(List<Map<String, Object>>) extMaint.get("processMethod");
            if(!ObjectUtil.isEmpty(processMethod)){
                StringBuilder process = new StringBuilder();
                for (int i = 0; i < processMethod.size(); i++) {
                    if(i==processMethod.size()-1){
                        process.append(processMethod.get(i).get("name"));
                    }else {
                        process.append(processMethod.get(i).get("name")).append(",");
                    }
                }
                extMaint.put("processMethod",process.toString());
            }else {
                extMaint.put("processMethod","");
            }
            data.put("extMaint", extMaint);//外部维修
            Map<String, Object> handelContent = (Map<String, Object>) ITDept.get("handelContent");
            if(!ObjectUtil.isEmpty(handelContent)){
                List<Map<String, Object>> categories = (List<Map<String, Object>>) handelContent.get("categories");
                if(ObjectUtil.isEmpty(categories)){
                    handelContent.put("categories","");
                }else {
                    StringBuilder category = new StringBuilder();
                    for (int i = 0; i < categories.size(); i++) {
                        if(i==categories.size()-1){
                            category.append(categories.get(i).get("name"));
                        }else {
                            category.append(categories.get(i).get("name")).append(",");
                        }
                    }
                    handelContent.put("categories",category.toString());
                }

                List<Map<String, Object>> handelMethods = (List<Map<String, Object>>) handelContent.get("handelMethod");
                if(ObjectUtil.isEmpty(categories)){
                    handelContent.put("categories","");
                }else {
                    StringBuilder handelMethod = new StringBuilder();
                    for (int i = 0; i < handelMethods.size(); i++) {
                        if (i == handelMethods.size() - 1) {
                            handelMethod.append(handelMethods.get(i).get("name")).append(",");
                        } else {
                            handelMethod.append(handelMethods.get(i).get("name"));
                        }
                    }
                    handelContent.put("handelMethod", handelMethod.toString());
                }
            }else {
                handelContent.put("categories","");
                handelContent.put("handelMethod","");
            }
            data.put("handelContent", handelContent);//处理内容

            // 设置动态租户ID,默认000000（审批记录查询接口用到了租户ID）
            TenantHelper.setDynamic(StringUtils.blankToDefault(taskNodeDataVo.getTenantId(),"000000"));
            // 审批记录
            List<DcwsActHistoryInfoVo> historyRecords = dcwsIActProcessInstanceService.getHistoryRecord(taskNodeDataVo.getId());
            // 查询审批人昵称名称
            List<DcwsActHistoryInfoVo> collect = historyRecords.stream()
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
            byte[] pdfBytes = createApplyFilePDFService.generatePdf(templateName, data);
            String documentTypeId = "3";//TODO 需要维护
            String storagePathId = "6";
            String[] tags = new String[]{"16"};//TODO 在档案系统新增
            String objName = "服务申请-";
            Map<String, Object> documentServerParam = MapUtil.createMap(HashMap.class);
            documentServerParam.put(DOCUMENTTYPEID,documentTypeId);//档案系统文件类型
            documentServerParam.put(STORAGEPATHID,storagePathId);//档案系统文件路径
            documentServerParam.put(TAGS,tags);//档案系统文件标签
            String fileName = objName + taskNodeDataVo.getId() + ".pdf";
            resultMap.put(FILENAME,fileName);
            resultMap.put(PDFBYTES,pdfBytes);
            resultMap.put(DOCUMENTSERVERPARAM,documentServerParam);
        } catch (Exception e) {
            log.error("生成PDF失败:{}",e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // 清除动态租户ID
            TenantHelper.clearDynamic();
        }
        return resultMap;
    }
}
