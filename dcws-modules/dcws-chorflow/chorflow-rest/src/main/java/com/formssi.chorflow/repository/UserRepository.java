package com.formssi.chorflow.repository;

import com.formssi.chorflow.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findFirstByNameAndPwd(String name, String pwd);
}