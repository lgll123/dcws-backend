package com.formssi.chorflow.db;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.formssi.system.domain.SysDocInfo;
import com.formssi.system.domain.SysDocParam;
import com.formssi.system.domain.SysModule;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * 回滚化数据库 测试
 *
 * @author joey
 * @date 2025.1.7
 */
@Slf4j
public class DeleteDbTest extends DbBaseTest {

  private static List<Long> sysModuleIdList;

  @Test
  @Order(1)
  void deleteSysProject() {
    boolean removed = iProjectService.removeById(PROJECT_ID);
    Assertions.assertTrue(removed);
  }


  @Test
  @Order(2)
  void deleteSysModule() {

    Wrapper<SysModule> wrapper = new LambdaQueryWrapper<SysModule>().eq(SysModule::getProjectId,
        PROJECT_ID).select(SysModule::getId);

    sysModuleIdList = moduleService.listObjs(wrapper);
    if (CollectionUtil.isEmpty(sysModuleIdList)) {
      log.info("sysModuleIdList is empty");
      return;
    }
    log.info("sysModuleIdList:{}", sysModuleIdList);

    boolean removed = moduleService.removeByIds(sysModuleIdList);
    Assertions.assertTrue(removed);
  }

  @Test
  @Order(3)
  void deleteSysDocInfo() {
    if (CollectionUtil.isEmpty(sysModuleIdList)) {
      log.info("sysModuleIdList is empty");
      return;
    }
    Wrapper<SysDocInfo> wrapper = new LambdaQueryWrapper<SysDocInfo>().in(SysDocInfo::getModuleId,
        sysModuleIdList).select(SysDocInfo::getId);
    List<Long> sysDocInfoIdList = docInfoService.listObjs(wrapper);
    if (CollectionUtil.isEmpty(sysDocInfoIdList)) {
      log.info("sysDocInfoIdList is empty");
      return;
    }
    log.info("sysDocInfoIdList:{}", sysDocInfoIdList);

    boolean removed = docInfoService.removeByIds(sysDocInfoIdList);
    Assertions.assertTrue(removed);

    Wrapper<SysDocParam> docWrapper = new LambdaQueryWrapper<SysDocParam>().in(
        SysDocParam::getDocId, sysDocInfoIdList).select(SysDocParam::getId);
    List<Long> sysDocParamIdList = docParamService.listObjs(docWrapper);
    if (CollectionUtil.isEmpty(sysDocParamIdList)) {
      log.info("sysDocParamIdList is empty");
      return;
    }
    log.info("sysDocParamIdList:{}", sysDocParamIdList);

    removed = docParamService.removeByIds(sysDocParamIdList);
    Assertions.assertTrue(removed);

  }


}
