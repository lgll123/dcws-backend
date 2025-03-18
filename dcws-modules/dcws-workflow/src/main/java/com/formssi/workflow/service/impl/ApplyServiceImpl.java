package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.domain.event.ProcessEvent;
import com.formssi.common.core.domain.event.ProcessTaskEvent;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.service.WorkflowService;
import com.formssi.system.domain.vo.SealJsonVo;
import com.formssi.workflow.domain.DcwsSysFile;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.mapper.DcwsSysFileMapper;
import com.formssi.workflow.utils.DcwsDateUtils;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.vo.SealInfoVo;
import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.TaskNodeData;
import com.formssi.workflow.domain.TaskNodeDataHis;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.bo.TaskNodeDataQueryBo;
import com.formssi.workflow.domain.vo.TaskNodeDataHisVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.mapper.TaskNodeDataHisMapper;
import com.formssi.workflow.mapper.TaskNodeDataMapper;
import com.formssi.workflow.service.IApplyService;
import com.formssi.workflow.service.TaskSerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 申请Service业务层处理
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ApplyServiceImpl implements IApplyService {

    private final TaskNodeDataMapper taskNodeDataMapper;
    private final TaskNodeDataHisMapper taskNodeDataHisMapper;
    private final DcwsSysFileMapper dcwsSysFileMapper;
    private final WorkflowService workflowService;
    private final TaskSerialService taskSerialService;
    private static final String keys = "{'non_IT_assets_apply','IT_assets_apply','seal_apply','claim_apply'" +
            ",'data_apply','server_apply'}";

    /**
     * 查询申请
     */
    @Override
    public TaskNodeDataVo queryById(String id) {
        return taskNodeDataMapper.selectVoById(id);
    }

    /**
     * 根据任务ID查询申请表单信息
     */
    @Override
    public TaskNodeDataHisVo queryByTaskId(String taskId){
        LambdaQueryWrapper<TaskNodeDataHis> lqw = Wrappers.lambdaQuery();
        lqw.eq(TaskNodeDataHis::getTaskId, taskId);
        return taskNodeDataHisMapper.selectVoOne(lqw);
    }

    /**
     * 查询申请列表
     */
    @Override
    public TableDataInfo<TaskNodeDataVo> queryPageList(TaskNodeDataQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TaskNodeData> lqw = buildQueryWrapper(bo);
        Page<TaskNodeDataVo> result = taskNodeDataMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询用印台账表单
     */
    @Override
    public TableDataInfo<TaskNodeDataVo> queryPageSealList(TaskNodeDataQueryBo bo, PageQuery pageQuery) {
        List<TaskNodeDataVo> sealInfoList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        //查询已完成的
        bo.setStatus(BusinessStatusEnum.FINISH.getStatus());
        bo.setApplyType("22");
        LambdaQueryWrapper<TaskNodeData> lqw = buildQueryWrapper2(bo);
        List<TaskNodeDataVo> sealList = taskNodeDataMapper.selectVoList(lqw);
        if(!CollectionUtil.isEmpty(sealList)){
            sealList.forEach(e ->{
                String data = e.getApplyDetail();
                if(!StringUtils.isBlank(data)){
                    try {
                        List<SealJsonVo> list = mapper.readValue(data, new TypeReference<>() {});
                        if(!CollectionUtil.isEmpty(list)){
                            list.forEach(i ->{
                                TaskNodeDataVo taskNodeDataVo = new TaskNodeDataVo();
                                BeanUtils.copyProperties(e,taskNodeDataVo);
                                taskNodeDataVo.setSealJsonVo(i);
                                sealInfoList.add(taskNodeDataVo);
                            });
                        }
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            // 分页
            List<TaskNodeDataVo> pageData = paginateData(sealInfoList, pageQuery.getPageNum(), pageQuery.getPageSize());
            // 封装成 TableDataInfo
            TableDataInfo<TaskNodeDataVo> tableDataInfo = new TableDataInfo<>();
            tableDataInfo.setTotal(sealInfoList.size());
            tableDataInfo.setRows(pageData);
            tableDataInfo.setCode(200);
            tableDataInfo.setMsg("查询成功");

            return tableDataInfo;
        }else {
            return TableDataInfo.build();
        }
    }

    public <T> List<T> paginateData(List<T> data, int pageNum, int pageSize) {
        int total = data.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex  + pageSize, total);
        return data.subList(fromIndex,  toIndex);
    }

    /**
     * 查询申请列表
     */
    @Override
    public List<TaskNodeDataVo> queryList(TaskNodeDataQueryBo bo) {
        LambdaQueryWrapper<TaskNodeData> lqw = buildQueryWrapper(bo);
        return taskNodeDataMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TaskNodeData> buildQueryWrapper(TaskNodeDataQueryBo bo) {
        LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getApplyDate()!=null, TaskNodeData::getApplyDate, bo.getApplyDate());
        lqw.like(StringUtils.isNotBlank(bo.getApplyDept()), TaskNodeData::getApplyDept, bo.getApplyDept());
        lqw.like(StringUtils.isNotBlank(bo.getApplicant()), TaskNodeData::getApplicant, bo.getApplicant());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyType()), TaskNodeData::getApplyType, bo.getApplyType());
        lqw.in(TaskNodeData::getStatus, Arrays.asList("back","draft"));
        lqw.eq(TaskNodeData::getCreateBy, LoginHelper.getUserId());
        lqw.orderByDesc(DcwsBaseEntity::getCreateTime);
        return lqw;
    }

    private LambdaQueryWrapper<TaskNodeData> buildQueryWrapper2(TaskNodeDataQueryBo bo) {
        LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getApplyDate()!=null, TaskNodeData::getApplyDate, bo.getApplyDate());
        lqw.like(StringUtils.isNotBlank(bo.getApplyDept()), TaskNodeData::getApplyDept, bo.getApplyDept());
        lqw.like(StringUtils.isNotBlank(bo.getApplicant()), TaskNodeData::getApplicant, bo.getApplicant());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyType()), TaskNodeData::getApplyType, bo.getApplyType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), TaskNodeData::getStatus, bo.getStatus());
        lqw.orderByDesc(DcwsBaseEntity::getCreateTime);
        return lqw;
    }

    /**
     * 新增申请
     */
    @Override
    public TaskNodeDataVo insertByBo(TaskNodeDataBo bo) {
        TaskNodeData add = MapstructUtils.convert(bo, TaskNodeData.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        }
        String id = taskSerialService.getTaskSerial(bo.getApplyType(), DcwsDateUtils.dateTime());
        add.setId(id);
        boolean flag = taskNodeDataMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return MapstructUtils.convert(add, TaskNodeDataVo.class);
    }

    /**
     * 修改申请
     */
    @Override
    public TaskNodeDataVo updateByBo(TaskNodeDataBo bo) {
        TaskNodeData update = MapstructUtils.convert(bo, TaskNodeData.class);
        taskNodeDataMapper.updateById(update);
        return MapstructUtils.convert(update, TaskNodeDataVo.class);
    }

    /**
     * 批量删除申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<String> ids) {
        List<String> idList = StreamUtils.toList(ids, String::valueOf);
        workflowService.deleteRunAndHisInstance(idList);
        return taskNodeDataMapper.deleteByIds(ids) > 0;
    }
    /**
     * 查询申请单PDF
     */
    @Override
    public List<DcwsSysFileVo> getApplyPDF(String id){
        LambdaQueryWrapper<DcwsSysFile> lqw = Wrappers.lambdaQuery();
        lqw.eq(id!=null, DcwsSysFile::getTaskNodeDataId, id);
        return dcwsSysFileMapper.selectVoList(lqw);
    }

    /**
     * 总体流程监听(例如: 提交 退回 撤销 终止 作废等)
     * 正常使用只需#processEvent.key=='leave1'
     * 示例为了方便则使用startsWith匹配了全部示例key
     *
     * @param processEvent 参数
     */
    @EventListener(condition = keys+".contains(#processEvent.key)")
    public void processHandler(ProcessEvent processEvent) {
        log.info("当前任务执行了{}", processEvent.toString());
        TaskNodeData taskNodeData = taskNodeDataMapper.selectById(processEvent.getBusinessKey());
        taskNodeData.setStatus(processEvent.getStatus());
        if (processEvent.isSubmit()) {
            taskNodeData.setStatus(processEvent.getStatus());
        }
        taskNodeDataMapper.updateById(taskNodeData);
    }

    /**
     * 执行办理任务监听
     * 示例：也可通过  @EventListener(condition = "#processTaskEvent.key=='leave1'")进行判断
     * 在方法中判断流程节点key
     * if ("xxx".equals(processTaskEvent.getTaskDefinitionKey())) {
     * //执行业务逻辑
     * }
     *
     * @param processTaskEvent 参数
     */
    @EventListener(condition = keys+".contains(#processEvent.key)")
    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
        log.info("当前任务执行了{}", processTaskEvent.toString());
        TaskNodeData taskNodeData = taskNodeDataMapper.selectById(processTaskEvent.getBusinessKey());
        taskNodeData.setStatus(BusinessStatusEnum.WAITING.getStatus());
        TaskNodeDataBo taskNodeDataBo = new TaskNodeDataBo();
        if (CollUtil.isNotEmpty(processTaskEvent.getVariables())) {
            Map<String, Object> variables = processTaskEvent.getVariables();
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null){
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        taskNodeData.setApplyDetail(taskNodeDataBo.getApplyDetail());
        taskNodeData.setTaskId(processTaskEvent.getTaskId());
        taskNodeDataMapper.updateById(taskNodeData);
        QueryWrapper<TaskNodeDataHis> query = Wrappers.query();
        query.eq("task_id",processTaskEvent.getTaskId());
        TaskNodeDataHisVo taskNodeDataHisVo = taskNodeDataHisMapper.selectVoOne(query);
        taskNodeDataBo.setTaskNodeDataId(taskNodeData.getId());
        if(taskNodeDataHisVo!=null){
            TaskNodeDataHis taskNodeDataHis = new TaskNodeDataHis();
            taskNodeDataHis.setApplyDetail(taskNodeDataBo.getApplyDetail());
            taskNodeDataHis.setStatus(taskNodeData.getStatus());
            taskNodeDataHis.setTaskNodeDataId(taskNodeDataBo.getId());
            taskNodeDataHis.setTaskId(processTaskEvent.getTaskId());
            taskNodeDataHis.setAssetUserId(taskNodeDataBo.getAssetUserId());
            taskNodeDataHisMapper.updateById(taskNodeDataHis);
        }else {
            TaskNodeDataHis taskNodeDataHis = new TaskNodeDataHis();
            taskNodeDataHis.setApplicant(taskNodeDataBo.getApplicant());
            taskNodeDataHis.setApplicantId(taskNodeDataBo.getApplicantId());
            taskNodeDataHis.setAssetUserId(taskNodeDataBo.getAssetUserId());
            taskNodeDataHis.setApplyDate(taskNodeDataBo.getApplyDate());
            taskNodeDataHis.setApplyDetail(taskNodeDataBo.getApplyDetail());
            taskNodeDataHis.setApplyReson(taskNodeDataBo.getApplyReson());
            taskNodeDataHis.setApplyRemarks(taskNodeDataBo.getApplyRemarks());
            taskNodeDataHis.setRequiredDateType(taskNodeDataBo.getRequiredDateType());
            taskNodeDataHis.setCompleteDate(taskNodeDataBo.getCompleteDate());
            taskNodeDataHis.setRequiredDesc(taskNodeDataBo.getRequiredDesc());
            taskNodeDataHis.setApplyDept(taskNodeDataBo.getApplyDept());
            taskNodeDataHis.setApplyType(taskNodeDataBo.getApplyType());
            taskNodeDataHis.setStatus(taskNodeData.getStatus());
            taskNodeDataHis.setTaskNodeDataId(taskNodeData.getId());
            taskNodeDataHis.setTaskId(processTaskEvent.getTaskId());
            taskNodeDataHis.setCheckTo(taskNodeData.getCheckTo());
            taskNodeDataHis.setApplyContentType(taskNodeData.getApplyContentType());
            taskNodeDataHisMapper.insert(taskNodeDataHis);
        }

    }
}
