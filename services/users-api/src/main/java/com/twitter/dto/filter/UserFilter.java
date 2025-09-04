package com.twitter.dto.filter;

import com.twitter.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record UserFilter(String firstNameContains, String lastNameContains, String email, String username) {
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(emailSpec())
            .and(usernameSpec());
    }

    private Specification<User> firstNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(firstNameContains)
            ? cb.like(root.get("firstName"), "%" + firstNameContains + "%")
            : null);
    }

    private Specification<User> lastNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(lastNameContains)
            ? cb.like(root.get("lastName"), "%" + lastNameContains + "%")
            : null);
    }

    private Specification<User> emailSpec() {
        return ((root, query, cb) -> StringUtils.hasText(email)
            ? cb.like(root.get("email"), email)
            : null);
    }

    private Specification<User> usernameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(username)
            ? cb.like(root.get("username"), username)
            : null);
    }
}
