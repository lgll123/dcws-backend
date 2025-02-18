package com.formssi.ureport2.provider;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import com.formssi.ureport2.entity.SysUreportFile;
import com.formssi.ureport2.service.ISysUreportFileService;
import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mysql 报表存储提供者
 *
 * @author zhangmiao
 */
@Slf4j
@Setter
@Component
@ConfigurationProperties(prefix = "ureport.mysql.provider")
public class MySQLProvider implements ReportProvider {

  private static final String suffix = ".ureport.xml";

  private static final String NAME = "mysql-provider";

  private String prefix = "formssi:";

  private boolean disabled;

  @Resource
  private ISysUreportFileService iSysUreportFileService;

  @Override
  public InputStream loadReport(String file) {
    file = getRemovePrefixName(file);
    Wrapper<SysUreportFile> wrapper = new LambdaQueryWrapper<SysUreportFile>()
        .eq(SysUreportFile::getName, file);
    SysUreportFile sysUreportFile = iSysUreportFileService.getOne(wrapper);
    if (sysUreportFile != null) {
      byte[] content = sysUreportFile.getContent();
      return new ByteArrayInputStream(content);
    } else {
      return null;
    }
  }

  @Override
  public void deleteReport(String file) {
    file = getRemovePrefixName(file);
    Wrapper<SysUreportFile> wrapper = new LambdaQueryWrapper<SysUreportFile>()
        .eq(SysUreportFile::getName, file);
    iSysUreportFileService.remove(wrapper);
  }

  @Override
  public List<ReportFile> getReportFiles() {
    List<SysUreportFile> ureportFiles = iSysUreportFileService.list();
    List<ReportFile> reportFileList = new ArrayList<>();
    for (SysUreportFile entity : ureportFiles) {
      Date updateDate = Date.from(entity.getUpdateTime()
          .toLocalDate()
          .atStartOfDay()
          .atZone(ZoneId.systemDefault())
          .toInstant());
      reportFileList.add(new ReportFile(entity.getName(), updateDate));
    }
    return reportFileList;
  }

  @Override
  public void saveReport(String file, String content) {
    file = getRemovePrefixName(file);
    Wrapper<SysUreportFile> wrapper = new LambdaQueryWrapper<SysUreportFile>()
        .eq(SysUreportFile::getName, file);
    SysUreportFile sysUreportFile = iSysUreportFileService.getOne(wrapper);
    if (sysUreportFile == null) {
      sysUreportFile = new SysUreportFile();
      sysUreportFile.setName(file);
      sysUreportFile.setFileName(getFileName(file));
      sysUreportFile.setContent(content.getBytes());
      sysUreportFile.setCreateTime(LocalDateTime.now());
      sysUreportFile.setUpdateTime(LocalDateTime.now());
      iSysUreportFileService.save(sysUreportFile);
    } else {
      sysUreportFile.setFileName(getFileName(file));
      sysUreportFile.setContent(content.getBytes());
      sysUreportFile.setUpdateTime(LocalDateTime.now());
      iSysUreportFileService.update(sysUreportFile, wrapper);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean disabled() {
    return disabled;
  }

  @Override
  public String getPrefix() {
    return prefix;
  }


  private String getRemovePrefixName(String file) {
    if (StringUtils.isNotBlank(prefix) && file.startsWith(prefix)) {
      file = file.substring(prefix.length());
    }
    return file;
  }

  private String getFileName(String file) {
    if (StringUtils.isNotBlank(prefix) && file.endsWith(suffix)) {
      file = file.substring(0, file.length() - suffix.length());
    }
    return file;
  }

}
