package com.twitter.util;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for generating random data using Datafaker library.
 *
 * @author geron
 * @version 1.0
 */
@Component
@Slf4j
public class RandomDataGenerator {

    private final Faker faker = new Faker();

    /**
     * Generates a unique login name for user authentication.
     * <p>
     * The login is generated using Datafaker's name generator (firstName + lastName)
     * and made unique by appending timestamp and UUID suffix.
     * The result is truncated to 50 characters to comply with UserRequestDto constraints.
     *
     * @return unique login string (3-50 characters)
     */
    public String generateLogin() {
        String baseLogin = (faker.name().firstName() + "_" + faker.name().lastName()).toLowerCase()
            .replaceAll("[^a-z0-9_]", "");
        String uniqueSuffix = "_" + System.currentTimeMillis() + "_" +
            UUID.randomUUID().toString().substring(0, 8);
        String login = baseLogin + uniqueSuffix;

        if (login.length() > 50) {
            login = login.substring(0, 50);
        }

        log.debug("Generated login: {}", login);
        return login;
    }

    /**
     * Generates a unique email address for user registration.
     * <p>
     * The email is generated using Datafaker's internet email generator
     * and made unique by inserting timestamp before the @ symbol.
     * The result is guaranteed to be a valid email format.
     *
     * @return unique email string in valid email format
     */
    public String generateEmail() {
        String baseEmail = faker.internet().emailAddress();
        String uniquePart = "_" + System.currentTimeMillis();
        String email = baseEmail.replace("@", uniquePart + "@");

        log.debug("Generated email: {}", email);
        return email;
    }

    /**
     * Generates a random first name for user profile.
     * <p>
     * The first name is generated using Datafaker's name generator.
     * This field is optional in UserRequestDto, so it may be null in some cases,
     * but this method always returns a non-null value.
     *
     * @return random first name string (maybe null if not needed)
     */
    public String generateFirstName() {
        String firstName = faker.name().firstName();
        log.debug("Generated first name: {}", firstName);
        return firstName;
    }

    /**
     * Generates a random last name for user profile.
     * <p>
     * The last name is generated using Datafaker's name generator.
     * This field is optional in UserRequestDto, so it may be null in some cases,
     * but this method always returns a non-null value.
     *
     * @return random last name string (maybe null if not needed)
     */
    public String generateLastName() {
        String lastName = faker.name().lastName();
        log.debug("Generated last name: {}", lastName);
        return lastName;
    }

    /**
     * Generates a random password for user authentication.
     * <p>
     * The password is generated using a combination of Datafaker's text generators
     * to ensure it meets the following constraints:
     * - Minimum length: 8 characters (required by UserRequestDto)
     * - Maximum length: 20 characters
     * - Includes digits, lowercase, and uppercase letters
     *
     * @return random password string (8-20 characters)
     */
    public String generatePassword() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String part1 = firstName.substring(0, Math.min(3, firstName.length()));
        String part2 = lastName.substring(0, Math.min(2, lastName.length()));
        String numbers = String.valueOf(faker.number().numberBetween(1000, 9999));
        String password = (part1 + part2 + numbers).replaceAll("[^a-zA-Z0-9]", "");

        if (!password.isEmpty()) {
            password = password.substring(0, 1).toUpperCase() +
                (password.length() > 1 ? password.substring(1).toLowerCase() : "") + numbers;
        } else {
            password = "A" + numbers;
        }

        if (password.length() < 8) {
            password = password + faker.number().numberBetween(10, 99);
        }
        if (password.length() > 20) {
            password = password.substring(0, 20);
        }

        log.debug("Generated password (length: {})", password.length());
        return password;
    }

    /**
     * Generates random tweet content with length constraint.
     * <p>
     * The content is generated using Datafaker's lorem text generator
     * (sentence or paragraph) and truncated to 280 characters maximum
     * to comply with CreateTweetRequestDto constraints.
     * If truncation occurs, "..." is appended to indicate truncation.
     * The result is guaranteed to be at least 1 character long.
     *
     * @return random tweet content string (1-280 characters)
     */
    public String generateTweetContent() {
        String content = faker.lorem().sentence();
        if (content.length() < 10) {
            content = faker.lorem().paragraph();
        }

        if (content.length() > 280) {
            content = content.substring(0, 277) + "...";
        }

        if (content.isEmpty()) {
            content = faker.lorem().word();
        }

        log.debug("Generated tweet content (length: {})", content.length());
        return content;
    }
}
