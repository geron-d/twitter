package com.twitter.dto.filter;

import com.twitter.entity.User;
import com.twitter.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record UserFilter(String firstNameContains, String lastNameContains, String email, String login, UserRole role) {
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(emailSpec())
            .and(loginSpec())
            .and(roleSpec());
    }

    private Specification<User> firstNameContainsSpec() {
        return ((root, _, cb) -> StringUtils.hasText(firstNameContains)
            ? cb.like(root.get("firstName"), "%" + firstNameContains + "%")
            : null);
    }

    private Specification<User> lastNameContainsSpec() {
        return ((root, _, cb) -> StringUtils.hasText(lastNameContains)
            ? cb.like(root.get("lastName"), "%" + lastNameContains + "%")
            : null);
    }

    private Specification<User> emailSpec() {
        return ((root, _, cb) -> StringUtils.hasText(email)
            ? cb.like(root.get("email"), email)
            : null);
    }

    private Specification<User> loginSpec() {
        return ((root, _, cb) -> StringUtils.hasText(login)
            ? cb.like(root.get("login"), login)
            : null);
    }

    private Specification<User> roleSpec() {
        return ((root, _, cb) -> role != null
            ? cb.equal(root.get("role"), role)
            : null);
    }
}
