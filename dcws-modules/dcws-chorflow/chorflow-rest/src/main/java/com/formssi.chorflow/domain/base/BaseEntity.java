package com.formssi.chorflow.domain.base;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Comment;

/**
 * 自定义 父类实体
 *
 * @author joey
 * @date 2024.11.25
 */
@MappedSuperclass
@Data
@FieldNameConstants
public class BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("主键")
  protected Long id;

  @Comment("创建时间")
  private LocalDateTime createTime;

  @Comment("创建人")
  private Long createBy;

  @Comment("更新时间")
  private LocalDateTime updateTime;

  @Comment("更新人")
  private Long updateBy;

  @Comment("逻辑删除: 1-已删除 0-未删除")
  private Boolean deleted;


  @PrePersist
  protected void prePersist() {
    createTime = LocalDateTime.now();
    updateTime = LocalDateTime.now();
  }


  @PreUpdate
  protected void preUpdate() {
    updateTime = LocalDateTime.now();
  }


  @PreRemove
  protected void preRemove() {
    deleted = true;
  }
}
