package com.formssi.chorflow.service;

import com.formssi.chorflow.domain.User;
import com.formssi.chorflow.domain.UserAccount;
import com.formssi.chorflow.domain.req.UserScopeData;
import com.formssi.chorflow.repository.UserAccountRepository;
import com.formssi.chorflow.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAccountService {

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;


  public String login(User user) {
    Optional<User> optional = userRepository.findFirstByNameAndPwd(user.getName(), user.getPwd());
    if (optional.isPresent()) {
      return UUID.randomUUID().toString();
    }
    throw new RuntimeException("login failed");
  }

  public Long getBalance(Long userId) {
    Optional<UserAccount> one = userAccountRepository.findFirstByUserId(userId);
    return one.map(UserAccount::getBalance).orElse(null);
  }

  public Long cost(UserScopeData userScopeData) {
    Long userId = userScopeData.getUserId();
    Long consumptionAmount = userScopeData.getConsumptionAmount();
    if (ObjectUtils.anyNull(userId, consumptionAmount)) {
      throw new RuntimeException("param is null");
    }
    Optional<UserAccount> one = userAccountRepository.findById(userId);
    if (one.isPresent()) {
      UserAccount get = one.get();
      long balance = get.getBalance() - consumptionAmount;
      get.setBalance(balance);
      userAccountRepository.save(get);
      return balance;
    }
    throw new RuntimeException("user not exist");
  }

  public Long deposit(UserScopeData userScopeData) {
    Long userId = userScopeData.getUserId();
    Long depositAmount = userScopeData.getDepositAmount();
    if (ObjectUtils.anyNull(userId, depositAmount)) {
      throw new RuntimeException("param is null");
    }
    Optional<UserAccount> one = userAccountRepository.findById(userId);
    if (one.isPresent()) {
      UserAccount get = one.get();
      long balance = get.getBalance() + depositAmount;
      get.setBalance(balance);
      userAccountRepository.save(get);
      return balance;
    }
    throw new RuntimeException("user not exist");
  }

}