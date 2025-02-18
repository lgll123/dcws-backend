package com.formssi.chorflow.service;

import com.formssi.chorflow.domain.User;
import com.formssi.chorflow.domain.UserRole;
import com.formssi.chorflow.repository.UserAccountRepository;
import com.formssi.chorflow.repository.UserRepository;
import com.formssi.chorflow.repository.UserRoleRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private UserRoleRepository userRoleRepository;

  @Autowired
  private UserAccountRepository userAccountRepository;

  public User get(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  public User verify(User user) {
    return userRepository.findFirstByNameAndPwd(user.getName(), user.getPwd()).orElse(null);
  }

  public boolean isAdmin(Long id) {
    Optional<UserRole> one = userRoleRepository.findFirstByIsAdminAndUserId(true, id);
    if (one.isPresent()) {
      return one.get().getIsAdmin();
    }
    return false;
  }

  public User saveUser(User user) {
    return userRepository.save(user);
  }

  public List<User> list() {
    return userRepository.findAll();
  }

  public Page<User> page(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }

  public void deleteUser(Long id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
    } else {
      throw new RuntimeException("Invalid User ID");
    }
  }

  public User updateUser(Long id, User user) {
    Optional<User> customer = userRepository.findById(id);
    if (customer.isPresent()) {
      User entity = customer.get();
      entity.setId(id);
      entity.setName(user.getName());
      entity.setPwd(user.getPwd());
      entity.setAge(user.getAge());
      return userRepository.save(entity);
    } else {
      throw new RuntimeException("Invalid User ID");
    }
  }
}