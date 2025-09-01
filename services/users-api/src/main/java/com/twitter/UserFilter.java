package com.twitter;

import com.twitter.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record UserFilter(String firstNameContains, String secondNameContains) {
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(secondNameContainsSpec());
    }

    private Specification<User> firstNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(firstNameContains)
            ? cb.like(root.get("firstName"), "%" + firstNameContains + "%")
            : null);
    }

    private Specification<User> secondNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(secondNameContains)
            ? cb.like(root.get("secondName"), "%" + secondNameContains + "%")
            : null);
    }
}
