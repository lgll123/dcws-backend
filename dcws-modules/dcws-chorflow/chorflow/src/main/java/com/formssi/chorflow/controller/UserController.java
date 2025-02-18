package com.formssi.chorflow.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import com.formssi.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程编排 测试接口
 *
 * @author lijun
 * @date 2025-01-03
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

  private static Map<String, User> userMap = new LinkedHashMap<>();

  @PostMapping("/register")
  @Description("注册")
  @Operation(summary = "注册")
  public R<User> register(@RequestBody @Validated User user) {
    user.setBalance(0L);
    String userId = UUID.randomUUID().toString();
    user.setUserId(userId);
    userMap.put(userId, user);
    return R.ok(user);
  }

  @PostMapping("/login")
  @Description("登录")
  @Operation(summary = "登录")
  public R<String> login(@RequestBody @Validated User user) {
    Optional<User> first = userMap.values().stream()
        .filter(u -> u.getName().equals(user.getName()) && u.getPwd().equals(user.getPwd()))
        .findFirst();
    if (first.isPresent()) {
      // generate token
      User u = first.get();
      String token = UUID.randomUUID().toString();
      userMap.put(token, u);
      userMap.remove(u.getUserId());
      return R.ok(token);
    }
    return R.fail("user not exist");
  }

  @PostMapping("/cost")
  @Description("消费")
  @Operation(summary = "消费")
  public R<User> cost(@RequestHeader("token") String token, @RequestBody User user) {
    User u = userMap.get(token);
    if (null != u) {
      List<CostVo> consumptions = user.getConsumptions();
      if (CollectionUtil.isNotEmpty(consumptions)) {
        long sum = consumptions.stream().mapToLong(CostVo::getCost).sum();
        u.setBalance(u.getBalance() - sum);
        userMap.put(token, u);
        return R.ok(u);
      }
      return R.ok(u);
    }
    return R.fail("user not exist");
  }

  @PostMapping("/deposit")
  @Description("储蓄")
  @Operation(summary = "储蓄")
  public R<User> deposit(@RequestHeader("token") String token, User user) {
    User u = userMap.get(token);
    if (null != u) {
      u.setBalance(u.getBalance() + user.getDeposit());
      userMap.put(token, u);
      return R.ok(u);
    }
    return R.fail("user not exist");
  }

  @GetMapping("/getBalance")
  @Description("获取余额")
  @Operation(summary = "获取余额")
  public R<Long> getBalance(@RequestHeader("token") String token) {
    User u = userMap.get(token);
    if (null != u) {
      return R.ok(u.getBalance());
    }
    return R.fail("user not exist");
  }
}


@Data
class User {

  private String userId;
  @NotBlank
  private String name;
  @NotBlank
  private String pwd;

  private Long balance;

  private List<CostVo> consumptions;

  private Long deposit;

}


@Data
class CostVo {

  private String name;
  private Long cost;

}
