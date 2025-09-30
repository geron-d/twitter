package com.twitter.repository;

import com.twitter.entity.User;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    long countByRoleAndStatus(UserRole role, UserStatus status);
    
    boolean existsByLogin(String login);
    
    boolean existsByEmail(String email);
    
    boolean existsByLoginAndIdNot(String login, UUID id);
    
    boolean existsByEmailAndIdNot(String email, UUID id);
}
