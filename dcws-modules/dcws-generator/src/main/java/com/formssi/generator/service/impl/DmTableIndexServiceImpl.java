package com.formssi.generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.generator.domain.DmTableIndex;
import com.formssi.generator.mapper.DmTableIndexMapper;
import com.formssi.generator.service.IDmTableIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 索引 服务层实现
 *
 * @author Shen Tao
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmTableIndexServiceImpl extends ServiceImpl<DmTableIndexMapper, DmTableIndex> implements IDmTableIndexService {

}
