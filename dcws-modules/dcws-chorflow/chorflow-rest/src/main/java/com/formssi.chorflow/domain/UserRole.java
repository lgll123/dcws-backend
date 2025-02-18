package com.formssi.chorflow.domain;

import com.formssi.chorflow.domain.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * 用户 角色 表
 *
 * @author joey
 * @date 2024.11.19
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chor_user_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class UserRole extends BaseEntity {


  private Long userId;

  private Boolean isAdmin;


}
