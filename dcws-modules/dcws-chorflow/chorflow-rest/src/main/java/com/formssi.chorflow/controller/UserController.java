package com.formssi.chorflow.controller;

import com.formssi.chorflow.domain.User;
import com.formssi.chorflow.service.UserService;
import com.formssi.common.core.domain.R;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserService userService;

  @PostMapping("/verify")
  public R<User> verify(@RequestBody User user) {
    return R.ok(userService.verify(user));
  }

  @GetMapping("/isAdmin")
  public  R<Boolean> isAdmin(User user) {
    return R.ok(userService.isAdmin(user.getId()));
  }

  @GetMapping("/list")
  public  R<List<User>> list() {
    return R.ok(userService.list());
  }

  
  @GetMapping("/page")
  public  R<Page<User>> page(Pageable pageable) {
    return R.ok(userService.page(pageable));
  }


  @GetMapping("/get")
  public  R<User> get(User user) {
    return R.ok(userService.get(user.getId()));
  }
}
