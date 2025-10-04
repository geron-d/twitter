package com.twitter.dto.filter;

import com.twitter.common.enums.UserRole;
import com.twitter.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Filter DTO for user search and filtering operations.
 * <p>
 * This record provides a convenient way to specify filtering criteria for user
 * queries. It supports partial matching for name fields and exact matching for
 * email, login, and role fields. The filter can be converted to a JPA Specification
 * for database queries using Spring Data JPA.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * UserFilter filter = new UserFilter("John", "Doe", null, null, UserRole.USER);
 * Specification<User> spec = filter.toSpecification();
 * List<User> users = userRepository.findAll(spec);
 * }</pre>
 *
 * @author geron
 * @version 1.0
 * @param firstNameContains partial match for first name (can be null)
 * @param lastNameContains partial match for last name (can be null)
 * @param email exact match for email address (can be null)
 * @param login exact match for login name (can be null)
 * @param role exact match for user role (can be null)
 */
public record UserFilter(String firstNameContains, String lastNameContains, String email, String login, UserRole role) {
    /**
     * Converts this filter to a JPA Specification for database queries.
     * <p>
     * Only non-null and non-empty filter values are included in the resulting specification.
     *
     * @return JPA Specification combining all active filter criteria
     */
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(emailSpec())
            .and(loginSpec())
            .and(roleSpec());
    }

    /**
     * Creates a specification for first name partial matching.
     * <p>
     * Returns null if the filter value is null or empty.
     *
     * @return JPA Specification for first name filtering or null if no filter
     */
    private Specification<User> firstNameContainsSpec() {
        return ((root, _, cb) -> StringUtils.hasText(firstNameContains)
            ? cb.like(root.get("firstName"), "%" + firstNameContains + "%")
            : null);
    }

    /**
     * Creates a specification for last name partial matching.
     * <p>
     * Returns null if the filter value is null or empty.
     *
     * @return JPA Specification for last name filtering or null if no filter
     */
    private Specification<User> lastNameContainsSpec() {
        return ((root, _, cb) -> StringUtils.hasText(lastNameContains)
            ? cb.like(root.get("lastName"), "%" + lastNameContains + "%")
            : null);
    }

    /**
     * Creates a specification for email exact matching.
     * <p>
     * Returns null if the filter value is null or empty.
     *
     * @return JPA Specification for email filtering or null if no filter
     */
    private Specification<User> emailSpec() {
        return ((root, _, cb) -> StringUtils.hasText(email)
            ? cb.like(root.get("email"), email)
            : null);
    }

    /**
     * Creates a specification for login exact matching.
     * <p>
     * Returns null if the filter value is null or empty.
     *
     * @return JPA Specification for login filtering or null if no filter
     */
    private Specification<User> loginSpec() {
        return ((root, _, cb) -> StringUtils.hasText(login)
            ? cb.like(root.get("login"), login)
            : null);
    }

    /**
     * Creates a specification for role exact matching.
     * <p>
     * Returns null if the filter value is null or empty.
     *
     * @return JPA Specification for role filtering or null if no filter
     */
    private Specification<User> roleSpec() {
        return ((root, _, cb) -> role != null
            ? cb.equal(root.get("role"), role)
            : null);
    }
}
