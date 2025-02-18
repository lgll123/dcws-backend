package com.formssi.ureport2.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户表
 *
 * @author zhangmiao
 */
@Setter
@Getter
public class UserDTO {

  //主键
  private Long id;

  //用户名
  private String userName;

  //昵称
  private String nickName;

  //卡号
  private String account;

  //金额
  private Long amount;

}
