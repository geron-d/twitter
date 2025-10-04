package com.twitter.repository;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Repository interface for user data access operations.
 * <p>
 * This repository provides data access methods for User entities.
 * It extends JpaRepository and JpaSpecificationExecutor for standard
 * CRUD operations and dynamic query capabilities.
 *
 * @author geron
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    long countByRoleAndStatus(UserRole role, UserStatus status);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByLoginAndIdNot(String login, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
