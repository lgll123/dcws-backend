package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.utils.DateUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.workflow.service.TaskSerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.service.WorkflowService;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.TestLeave;
import com.formssi.workflow.domain.bo.TestLeaveBo;
import com.formssi.workflow.domain.vo.TestLeaveVo;
import com.formssi.workflow.mapper.TestLeaveMapper;
import com.formssi.workflow.service.ITestLeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 请假Service业务层处理
 *
 * @author may
 * @date 2023-07-21
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class TestLeaveServiceImpl implements ITestLeaveService {

    private final TestLeaveMapper baseMapper;
    private final WorkflowService workflowService;
    private final TaskSerialService taskSerialService;

    /**
     * 查询请假
     */
    @Override
    public TestLeaveVo queryById(String id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询请假列表
     */
    @Override
    public TableDataInfo<TestLeaveVo> queryPageList(TestLeaveBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TestLeave> lqw = buildQueryWrapper(bo);
        lqw.eq(TestLeave::getCreateBy, LoginHelper.getUserId());
        Page<TestLeaveVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询请假列表
     */
    @Override
    public List<TestLeaveVo> queryList(TestLeaveBo bo) {
        LambdaQueryWrapper<TestLeave> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TestLeave> buildQueryWrapper(TestLeaveBo bo) {
        LambdaQueryWrapper<TestLeave> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getLeaveType()), TestLeave::getLeaveType, bo.getLeaveType());
        lqw.ge(bo.getStartLeaveDays() != null, TestLeave::getLeaveDays, bo.getStartLeaveDays());
        lqw.le(bo.getEndLeaveDays() != null, TestLeave::getLeaveDays, bo.getEndLeaveDays());
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return lqw;
    }

    /**
     * 新增请假
     */
    @Override
    public TestLeaveVo insertByBo(TestLeaveBo bo) {
        TestLeave add = MapstructUtils.convert(bo, TestLeave.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        }
        String id = taskSerialService.getTaskSerial("18", DateUtils.dateTime());
        add.setId(id);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return MapstructUtils.convert(add, TestLeaveVo.class);
    }

    /**
     * 修改请假
     */
    @Override
    public TestLeaveVo updateByBo(TestLeaveBo bo) {
        TestLeave update = MapstructUtils.convert(bo, TestLeave.class);
        baseMapper.updateById(update);
        return MapstructUtils.convert(update, TestLeaveVo.class);
    }

    /**
     * 批量删除请假
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<String> ids) {
        List<String> idList = StreamUtils.toList(ids, String::valueOf);
        workflowService.deleteRunAndHisInstance(idList);
        return baseMapper.deleteByIds(ids) > 0;
    }

//    /**
//     * 总体流程监听(例如: 提交 退回 撤销 终止 作废，撤回等)
//     * 正常使用只需#processEvent.key=='leave'
//     * 示例为了方便则使用startsWith匹配了全部示例key
//     *
//     * @param processEvent 参数
//     */
//    @EventListener(condition = "#processEvent.key.startsWith('leave')")
//    public void processHandler(ProcessEvent processEvent) {
//        log.info("当前任务执行了，总体流程监听：{}", processEvent.toString());
//        TestLeave testLeave = baseMapper.selectById(Long.valueOf(processEvent.getBusinessKey()));
//        if (processEvent.isSubmit()) {
//            testLeave.setStatus(BusinessStatusEnum.WAITING.getStatus());
//        } else {
//            testLeave.setStatus(processEvent.getStatus());
//        }
//        baseMapper.updateById(testLeave);
//    }
//
//    /**
//     * 执行办理任务监听
//     * 示例：也可通过  @EventListener(condition = "#processTaskEvent.key=='leave'")进行判断
//     * 在方法中判断流程节点key
//     * if ("xxx".equals(processTaskEvent.getTaskDefinitionKey())) {
//     * //执行业务逻辑
//     * }
//     * @param processTaskEvent 参数
//     */
//    @EventListener(condition = "#processTaskEvent.key.startsWith('leave')")
//    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
//        // 所有demo案例的申请人节点id
//        String[] ids = {"Activity_14633hx", "Activity_19b1i4j", "Activity_0uscrk3",
//            "Activity_0uscrk3", "Activity_0x6b71j", "Activity_0zy3g6j", "Activity_06a55t0"};
//        if (StringUtils.equalsAny(processTaskEvent.getTaskDefinitionKey(), ids)) {
//            log.info("当前任务执行了，执行任务监听： {}", processTaskEvent);
//            TestLeave testLeave = baseMapper.selectById(Long.valueOf(processTaskEvent.getBusinessKey()));
//            testLeave.setStatus(BusinessStatusEnum.WAITING.getStatus());
//            baseMapper.updateById(testLeave);
//        }
//    }

}
