package com.twitter.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RandomDataGeneratorTest {

    private RandomDataGenerator randomDataGenerator;

    @BeforeEach
    void setUp() {
        randomDataGenerator = new RandomDataGenerator();
    }

    @Nested
    class GenerateLoginTests {

        @Test
        void generateLogin_ShouldReturnNonNullString() {
            String login = randomDataGenerator.generateLogin();

            assertThat(login).isNotNull();
        }

        @Test
        void generateLogin_ShouldReturnStringWithValidLength() {
            String login = randomDataGenerator.generateLogin();

            assertThat(login.length()).isGreaterThanOrEqualTo(3);
            assertThat(login.length()).isLessThanOrEqualTo(50);
        }

        @Test
        void generateLogin_ShouldReturnDifferentValuesOnMultipleCalls() {
            Set<String> logins = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                String login = randomDataGenerator.generateLogin();
                logins.add(login);
            }

            assertThat(logins.size()).isGreaterThan(1);
        }

        @Test
        void generateLogin_ShouldReturnUniqueValues() {
            Set<String> logins = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                String login = randomDataGenerator.generateLogin();
                assertThat(logins).doesNotContain(login);
                logins.add(login);
            }
        }

        @Test
        void generateLogin_ShouldContainOnlyValidCharacters() {
            String login = randomDataGenerator.generateLogin();

            assertThat(login).matches("^[a-z0-9_]+$");
        }
    }

    @Nested
    class GenerateEmailTests {

        @Test
        void generateEmail_ShouldReturnNonNullString() {
            String email = randomDataGenerator.generateEmail();

            assertThat(email).isNotNull();
        }

        @Test
        void generateEmail_ShouldReturnValidEmailFormat() {
            String email = randomDataGenerator.generateEmail();

            assertThat(email).contains("@");
            assertThat(email).matches("^[^@]+@[^@]+\\.[^@]+$");
        }

        @Test
        void generateEmail_ShouldReturnDifferentValuesOnMultipleCalls() {
            Set<String> emails = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                String email = randomDataGenerator.generateEmail();
                emails.add(email);
            }

            assertThat(emails.size()).isGreaterThan(1);
        }

        @Test
        void generateEmail_ShouldReturnUniqueValues() {
            Set<String> emails = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                String email = randomDataGenerator.generateEmail();
                assertThat(emails).doesNotContain(email);
                emails.add(email);
            }
        }
    }

    @Nested
    class GenerateFirstNameTests {

        @Test
        void generateFirstName_ShouldReturnNonNullString() {
            String firstName = randomDataGenerator.generateFirstName();

            assertThat(firstName).isNotNull();
        }

        @Test
        void generateFirstName_ShouldReturnNonEmptyString() {
            String firstName = randomDataGenerator.generateFirstName();

            assertThat(firstName).isNotEmpty();
        }

        @Test
        void generateFirstName_ShouldReturnDifferentValuesOnMultipleCalls() {
            Set<String> firstNames = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                String firstName = randomDataGenerator.generateFirstName();
                firstNames.add(firstName);
            }

            assertThat(firstNames.size()).isGreaterThan(1);
        }
    }

    @Nested
    class GenerateLastNameTests {

        @Test
        void generateLastName_ShouldReturnNonNullString() {
            String lastName = randomDataGenerator.generateLastName();

            assertThat(lastName).isNotNull();
        }

        @Test
        void generateLastName_ShouldReturnNonEmptyString() {
            String lastName = randomDataGenerator.generateLastName();

            assertThat(lastName).isNotEmpty();
        }

        @Test
        void generateLastName_ShouldReturnDifferentValuesOnMultipleCalls() {
            Set<String> lastNames = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                String lastName = randomDataGenerator.generateLastName();
                lastNames.add(lastName);
            }

            assertThat(lastNames.size()).isGreaterThan(1);
        }
    }

    @Nested
    class GeneratePasswordTests {

        @Test
        void generatePassword_ShouldReturnNonNullString() {
            String password = randomDataGenerator.generatePassword();

            assertThat(password).isNotNull();
        }

        @Test
        void generatePassword_ShouldReturnStringWithValidLength() {
            String password = randomDataGenerator.generatePassword();

            assertThat(password.length()).isGreaterThanOrEqualTo(8);
            assertThat(password.length()).isLessThanOrEqualTo(20);
        }

        @Test
        void generatePassword_ShouldReturnDifferentValuesOnMultipleCalls() {
            Set<String> passwords = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                String password = randomDataGenerator.generatePassword();
                passwords.add(password);
            }

            assertThat(passwords.size()).isGreaterThan(1);
        }

        @Test
        void generatePassword_ShouldContainAlphanumericCharacters() {
            String password = randomDataGenerator.generatePassword();

            assertThat(password).matches("^[a-zA-Z0-9]+$");
        }
    }

    @Nested
    class GenerateTweetContentTests {

        @Test
        void generateTweetContent_ShouldReturnNonNullString() {
            String content = randomDataGenerator.generateTweetContent();

            assertThat(content).isNotNull();
        }

        @Test
        void generateTweetContent_ShouldReturnStringWithValidLength() {
            String content = randomDataGenerator.generateTweetContent();

            assertThat(content.length()).isGreaterThanOrEqualTo(1);
            assertThat(content.length()).isLessThanOrEqualTo(280);
        }

        @Test
        void generateTweetContent_ShouldReturnNonEmptyString() {
            String content = randomDataGenerator.generateTweetContent();

            assertThat(content).isNotEmpty();
        }

        @Test
        void generateTweetContent_ShouldReturnDifferentValuesOnMultipleCalls() {
            Set<String> contents = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                String content = randomDataGenerator.generateTweetContent();
                contents.add(content);
            }

            assertThat(contents.size()).isGreaterThan(1);
        }

        @Test
        void generateTweetContent_WhenContentExceedsMaxLength_ShouldTruncate() {
            for (int i = 0; i < 100; i++) {
                String content = randomDataGenerator.generateTweetContent();
                assertThat(content.length()).isLessThanOrEqualTo(280);
            }
        }
    }
}
