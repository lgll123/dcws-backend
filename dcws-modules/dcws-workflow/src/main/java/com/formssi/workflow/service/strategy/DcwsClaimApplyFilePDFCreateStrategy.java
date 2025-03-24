package com.formssi.workflow.service.strategy;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.domain.vo.DcwsActHistoryInfoVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.service.DcwsIActProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

/**
 * dcws 费用报销申请单生成PDF数据处理接口策略
 *
 * @author yqh
 */
@Slf4j
@Service("claimApply")
@RequiredArgsConstructor
public class DcwsClaimApplyFilePDFCreateStrategy implements DcwsApplyFilePDFCreateStrategy<TaskNodeDataVo> {
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
            String applyDetail = taskNodeDataVo.getApplyDetail();
            Map<String,Object> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(applyDetail), Map.class);
            data.put("company",applyDetails.get("company"));
            data.put("department",applyDetails.get("department"));
            data.put("reimbursement",applyDetails.get("reimbursement"));
            data.put("projectCode",applyDetails.get("projectCode"));
            data.put("payee",applyDetails.get("payee"));
            data.put("customer",applyDetails.get("customer"));
            data.put("remark",applyDetails.get("remark"));
            data.put("costDetailCount",applyDetails.get("costDetailCount"));
            data.put("costDetailCountCapital",applyDetails.get("costDetailCountCapital"));
            data.put("invoiceDetailCount",applyDetails.get("invoiceDetailCount"));
            data.put("invoiceDetailCountCapital",applyDetails.get("invoiceDetailCountCapital"));
            //费用明细-costDetail、发票明细-invoiceDetail、附件明细-attachment
            if(!Objects.isNull(applyDetails.get("costDetail"))){
                List<Map<String,Object>> costDetail = objectMapper.readValue(JSONUtil.toJsonStr(applyDetails.get("costDetail")), List.class);
                for (Map<String, Object> costDetailMap : costDetail){
                    costDetailMap.put("purposeType",Objects.isNull(costDetailMap.get("purposeType")) ? "": costDetailMap.get("purposeType"));
                }
                data.put("costDetail", costDetail);
            }
            if(!Objects.isNull(applyDetails.get("invoiceDetail"))){
                List<Map<String,Object>> invoiceDetail = objectMapper.readValue(JSONUtil.toJsonStr(applyDetails.get("invoiceDetail")), List.class);
                for (Map<String, Object> invoiceMap : invoiceDetail){
                    invoiceMap.put("verify","Y".equals(invoiceMap.get("verify")) ? "通过":"不通过");
                }
                data.put("invoiceDetail", invoiceDetail);
            }
            if(!Objects.isNull(applyDetails.get("attachment"))){
                List<Map<String,Object>> attachment = objectMapper.readValue(JSONUtil.toJsonStr(applyDetails.get("attachment")), List.class);
                data.put("attachment", attachment);
            }
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
            String documentTypeId=null;
            String storagePathId=null;
            String[] tags=null;
            String objName=null;
            String applyType = taskNodeDataVo.getApplyType();//19 IT 21 非IT
            if("26".equals(applyType)){
                documentTypeId="3";
                storagePathId="9";
                tags=new String[]{"19"};
                objName="费用报销-";
            }
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
