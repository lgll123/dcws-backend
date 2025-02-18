package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.system.domain.SysProp;
import com.formssi.system.enums.PropTypeEnum;
import com.formssi.system.mapper.SysPropMapper;
import com.formssi.system.service.IPropService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lizhangyu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PropServiceImpl extends ServiceImpl<SysPropMapper, SysProp> implements IPropService {

    private final SysPropMapper sysPropMapper;

    @Override
    public void saveProps(Map<String, ?> props, Long refId, PropTypeEnum type) {
        saveProps(props, refId, type.getType());
    }

    /**
     * 保存属性参数
     * @param props 属性参数
     * @param refId 关联id
     * @param type 类型
     */
    public void saveProps(Map<String, ?> props, Long refId, byte type) {
        if (props == null || props.isEmpty()) {
            return;
        }

        List<SysProp> tobeSave = props.entrySet()
                .stream()
                .map(entry -> {
                    SysProp prop = new SysProp();
                    prop.setRefId(refId);
                    prop.setType(type);
                    prop.setName(entry.getKey());
                    prop.setVal(String.valueOf(entry.getValue()));
                    return prop;
                })
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(tobeSave)) {
            sysPropMapper.saveProps(tobeSave);
        }
    }

    public Map<String, String> getDocProps(Long docId) {
        if (docId == null) {
            return Collections.emptyMap();
        }
        return getProps(docId, PropTypeEnum.DOC_INFO_PROP.getType());
    }

    /**
     * 根据关联id和类型获取属性Map
     * @param refId 关联id
     * @param type 类型
     * @return 返回属性Map
     */
    public Map<String, String> getProps(Long refId, byte type) {
        return listProps(refId, type).stream().collect(Collectors.toMap(SysProp::getName, SysProp::getVal));
    }

    /**
     * 根据关联id和类型获取属性列表
     * @param refId 关联id
     * @param type 类型
     * @return 返回属性列表
     */
    public List<SysProp> listProps(Long refId, byte type) {
        if (refId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(SysProp::getRefId, refId)
                .eq(SysProp::getType, type)
                .list();
    }

    /**
     * 根据关联id和类型和属性名称获取属性
     * @param refId 关联id
     * @param type 类型
     * @param name 名称
     * @return 返回属性
     */
    public SysProp get(Long refId, byte type, String name) {
        return lambdaQuery().eq(SysProp::getRefId, refId)
                .eq(SysProp::getType, type)
                .eq(SysProp::getName, name)
                .one();
    }

}
