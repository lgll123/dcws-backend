package com.formssi.chorflow.domain.req;

import com.formssi.chorflow.domain.User;
import java.util.List;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 对象形式定义var数据域 用户作用域数据
 *
 * @author joey
 * @date 2024.11.19
 */
@Data
@FieldNameConstants(innerTypeName = "F")
public class UserScopeData  {

  private Long userId;
  private List<User> userList;
  private User user;
  private Boolean verify;
  // 是否是管理员
  private Boolean isAdmin;
  private String token;
  // 用户余额
  private Long balance;
  // 消费金额
  private Long consumptionAmount;
  // 储存金额
  private Long depositAmount;


}
