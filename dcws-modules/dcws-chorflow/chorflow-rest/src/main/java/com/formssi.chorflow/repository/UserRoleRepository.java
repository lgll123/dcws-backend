package com.formssi.chorflow.repository;

import com.formssi.chorflow.domain.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long>,
    QueryByExampleExecutor<UserRole> {

  Optional<UserRole> findFirstByIsAdmin(Boolean isAdmin);

  Optional<UserRole> findFirstByIsAdminAndUserId(Boolean isAdmin, Long userId);

}