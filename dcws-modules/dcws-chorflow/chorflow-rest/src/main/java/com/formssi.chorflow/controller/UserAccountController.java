package com.formssi.chorflow.controller;

import com.formssi.chorflow.domain.User;
import com.formssi.chorflow.domain.req.UserScopeData;
import com.formssi.chorflow.service.UserAccountService;
import com.formssi.common.core.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户 流程接口
 *
 * @author joey
 * @date 2024.11.19
 */
@RestController
@RequestMapping("/userAccount")
public class UserAccountController {

  @Autowired
  private UserAccountService userAccountService;

  @PostMapping("/login")
  public R<String> login(@RequestBody User user) {
    return R.ok(userAccountService.login(user));
  }

  @GetMapping("/getBalance")
  public R<Long> getBalance(Long userId) {
    return R.ok(userAccountService.getBalance(userId));
  }

  @PostMapping("/cost")
  public R<Long> cost(@RequestBody UserScopeData userScopeData) {
    return R.ok(userAccountService.cost(userScopeData));
  }

  @PostMapping("/deposit")
  public R<Long> deposit(@RequestBody UserScopeData userScopeData) {
    return R.ok(userAccountService.deposit(userScopeData));
  }
}