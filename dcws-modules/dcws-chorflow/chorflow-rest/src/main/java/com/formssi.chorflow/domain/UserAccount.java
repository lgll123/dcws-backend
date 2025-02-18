package com.formssi.chorflow.domain;

import com.formssi.chorflow.domain.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户 账户 表
 *
 * @author joey
 * @date 2024.11.19
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chor_user_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount extends BaseEntity {

  private Long userId;

  // 用户余额
  private Long balance;

}
