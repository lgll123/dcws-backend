package com.formssi.chorflow.repository;

import com.formssi.chorflow.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

  Optional<UserAccount> findFirstByUserId(Long userId);
}