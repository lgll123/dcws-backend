package com.formssi.ureport2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 报表文件表
 *
 * @author zhangmiao
 */
@Setter
@Getter
@TableName("sys_ureport_file")
public class SysUreportFile extends Model<SysUreportFile> {

  private static final long serialVersionUID = 1L;

  //主键
  private Long id;

  //文件名
  private String name;

  //下载文件名称
  private String fileName;

  //文件数据
  private byte[] content;

  //创建人
  private String createBy;

  //创建时间
  private LocalDateTime createTime;

  //修改人
  private String updateBy;

  //更新时间
  private LocalDateTime updateTime;

  //逻辑删除: 1-已删除 0-未删除
  private Boolean deleted;

  @Override
  public Serializable pkVal() {
    return this.id;
  }

  @Override
  public String toString() {
    return "SysUreportFile{" +
        "id = " + id +
        ", name = " + name +
        ", fileName = " + fileName +
        ", content = " + content +
        ", createBy = " + createBy +
        ", createTime = " + createTime +
        ", updateBy = " + updateBy +
        ", updateTime = " + updateTime +
        ", deleted = " + deleted +
        "}";
  }

}
