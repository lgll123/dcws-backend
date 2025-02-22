package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.domain.event.ProcessEvent;
import com.formssi.common.core.domain.event.ProcessTaskEvent;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.service.WorkflowService;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.Assets;
import com.formssi.workflow.domain.bo.AssetsBo;
import com.formssi.workflow.domain.vo.AssetsVo;
import com.formssi.workflow.mapper.AssetsMapper;
import com.formssi.workflow.service.IAssetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 物料申请Service业务层处理
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AssetsServiceImpl implements IAssetsService {

    private final AssetsMapper baseMapper;
    private final WorkflowService workflowService;

    /**
     * 查询物料申请
     */
    @Override
    public AssetsVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询物料申请列表
     */
    @Override
    public TableDataInfo<AssetsVo> queryPageList(AssetsBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Assets> lqw = buildQueryWrapper(bo);
        Page<AssetsVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询物料申请列表
     */
    @Override
    public List<AssetsVo> queryList(AssetsBo bo) {
        LambdaQueryWrapper<Assets> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Assets> buildQueryWrapper(AssetsBo bo) {
        LambdaQueryWrapper<Assets> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getCheckTo()), Assets::getCheckTo, bo.getCheckTo());
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return lqw;
    }

    /**
     * 新增物料申请
     */
    @Override
    public AssetsVo insertByBo(AssetsBo bo) {
        Assets add = MapstructUtils.convert(bo, Assets.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return MapstructUtils.convert(add, AssetsVo.class);
    }

    /**
     * 修改物料申请
     */
    @Override
    public AssetsVo updateByBo(AssetsBo bo) {
        Assets update = MapstructUtils.convert(bo, Assets.class);
        baseMapper.updateById(update);
        return MapstructUtils.convert(update, AssetsVo.class);
    }

    /**
     * 批量删除物料申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        List<String> idList = StreamUtils.toList(ids, String::valueOf);
        workflowService.deleteRunAndHisInstance(idList);
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 总体流程监听(例如: 提交 退回 撤销 终止 作废等)
     * 正常使用只需#processEvent.key=='leave1'
     * 示例为了方便则使用startsWith匹配了全部示例key
     *
     * @param processEvent 参数
     */
    @EventListener(condition = "#processEvent.key.startsWith('assets')")
    public void processHandler(ProcessEvent processEvent) {
//        log.info("当前任务执行了{}", processEvent.toString());
//        Assets assets = baseMapper.selectById(Long.valueOf(processEvent.getBusinessKey()));
//        assets.setStatus(processEvent.getStatus());
//        if (processEvent.isSubmit()) {
//            assets.setStatus(BusinessStatusEnum.WAITING.getStatus());
//        }
//        baseMapper.updateById(assets);
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
    @EventListener(condition = "#processTaskEvent.key.startsWith('assets')")
    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
        // 所有demo案例的申请人节点id
//        String[] ids = {"Activity_14633hx", "Activity_19b1i4j", "Activity_0uscrk3",
//            "Activity_0uscrk3", "Activity_0x6b71j", "Activity_0zy3g6j", "Activity_06a55t0"};
//        if (StringUtils.equalsAny(processTaskEvent.getTaskDefinitionKey(), ids)) {
//            log.info("当前任务执行了{}", processTaskEvent.toString());
//            Assets assets = baseMapper.selectById(Long.valueOf(processTaskEvent.getBusinessKey()));
//            assets.setStatus(BusinessStatusEnum.WAITING.getStatus());
//            baseMapper.updateById(assets);
//        }
    }
}
