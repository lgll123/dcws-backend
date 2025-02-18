package com.formssi.chorflow.service;

import cn.kstry.framework.core.annotation.NoticeVar;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import java.util.Random;

/**
 * 复杂 流程实现类
 *
 * @author joey
 * @date 2024.11.19
 */
@TaskComponent(name = "ComplexityComponent")
public class ComplexityComponent {

  @TaskService(name = "verify", desc = "verify")
  public void verify() {

  }

  @TaskService(name = "is_admin", desc = "is_admin")
  @NoticeVar(target = "isAdmin")
  public boolean isAdmin() {
    return new Random().nextBoolean();
  }

  @TaskService(name = "user_list", desc = "user_list")
  public void userList() {

  }

  @TaskService(name = "user_info", desc = "user_info")
  public void userInfo() {

  }


  @TaskService(name = "login", desc = "login")
  public void login() {

  }

  @TaskService(name = "cost", desc = "cost")
  public void cost() {

  }

  @TaskService(name = "deposit", desc = "deposit")
  public void deposit() {

  }

  @TaskService(name = "response", desc = "response")
  public void response() {

  }

  @TaskService(name = "get_balance", desc = "get_balance")
  @NoticeVar(target = "balance")
  public Integer getBalance() {
    return new Random().nextInt();
  }


}