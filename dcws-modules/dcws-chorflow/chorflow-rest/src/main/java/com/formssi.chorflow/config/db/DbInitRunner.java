package com.formssi.chorflow.config.db;

import com.formssi.chorflow.domain.User;
import com.formssi.chorflow.domain.UserAccount;
import com.formssi.chorflow.domain.UserRole;
import com.formssi.chorflow.repository.UserAccountRepository;
import com.formssi.chorflow.repository.UserRepository;
import com.formssi.chorflow.repository.UserRoleRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 数据初始化
 *
 * @author joey
 * @date 2024.11.25
 */
@Configuration
public class DbInitRunner implements CommandLineRunner {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserRoleRepository userRoleRepository;
  @Autowired
  private UserAccountRepository userAccountRepository;

  @Override
  public void run(String... args) {
    List<UserRole> userRoleList = new ArrayList<>();
    List<UserAccount> userAccountList = new ArrayList<>();

    String[] names = {"lee", "jack", "tom", "jerry", "lucy"};
    Boolean[] isAdminFlag = {true, false, false, false, false};
    for (int i = 0; i < 5; i++) {
      User user = new User();
      user.setName(names[i]);
      user.setPwd("123");
      user.setAge(22);
      userRepository.save(user);

      UserRole userRole = new UserRole();
      userRole.setUserId(user.getId());
      userRole.setIsAdmin(isAdminFlag[i]);
      userRoleList.add(userRole);

      UserAccount userAccount = new UserAccount();
      userAccount.setUserId(user.getId());
      userAccount.setBalance(100L);
      userAccountList.add(userAccount);
    }

    userRoleRepository.saveAll(userRoleList);
    userAccountRepository.saveAll(userAccountList);
  }


}
