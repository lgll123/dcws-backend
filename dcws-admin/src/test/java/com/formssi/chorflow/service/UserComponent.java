package com.formssi.chorflow.service;

import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.annotation.TaskService;
import java.util.Random;

/**
 * 用户 流程实现类
 *
 * @author joey
 * @date 2024.11.19
 */
// @TaskComponent(name = "UserComponent")
public class UserComponent {

  @TaskService(name = "login", desc = "login")
  public void login() {

  }

  @TaskService(name = "useInfo", desc = "useInfo")
  public void useInfo() {

  }

  @TaskService(name = "isAdmin", desc = "isAdmin")
  @NoticeVar(target = "isAdmin")
  public boolean isAdmin() {
    return new Random().nextBoolean();
  }

  @TaskService(name = "getBalance", desc = "getBalance")
  public void getBalance() {

  }

  @TaskService(name = "deposit", desc = "deposit")
  public void deposit() {

  }

  @TaskService(name = "cost", desc = "cost")
  public void cost() {

  }


  @TaskService(name = "transaction", desc = "transaction")
  public void transaction() {

  }


}