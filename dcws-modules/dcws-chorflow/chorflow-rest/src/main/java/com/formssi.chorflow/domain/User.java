package com.formssi.chorflow.domain;

import com.formssi.chorflow.domain.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

/**
 * 用户
 *
 * @author joey
 * @date 2024.11.19
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chor_user")
@Data
@FieldNameConstants
public class User extends BaseEntity {

  private String name;

  private String pwd;

  private Integer age;

}
