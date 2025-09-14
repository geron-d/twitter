package com.twitter.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Nested
    class GetSaltTests {

        @Test
        void shouldReturnNonNullSaltArray() {
            byte[] salt = PasswordUtil.getSalt();
            assertNotNull(salt);
        }

        @Test
        void shouldReturnSaltArrayWithCorrectLength() {
            byte[] salt = PasswordUtil.getSalt();
            assertEquals(16, salt.length);
        }

        @Test
        void shouldReturnDifferentSaltValuesOnMultipleCalls() {
            Set<String> saltValues = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                byte[] salt = PasswordUtil.getSalt();
                String saltString = Arrays.toString(salt);
                saltValues.add(saltString);
            }

            assertTrue(saltValues.size() > 1, "All salt values should be different");
        }

        @Test
        void shouldReturnSaltWithValidByteValues() {
            byte[] salt = PasswordUtil.getSalt();

            for (byte b : salt) {
                assertTrue(b >= Byte.MIN_VALUE && b <= Byte.MAX_VALUE,
                    "Salt bytes should be within valid byte range");
            }
        }

        @Test
        void shouldReturnSaltArrayWithNonZeroValues() {
            boolean hasNonZeroBytes = false;

            for (int i = 0; i < 10; i++) {
                byte[] salt = PasswordUtil.getSalt();
                for (byte b : salt) {
                    if (b != 0) {
                        hasNonZeroBytes = true;
                        break;
                    }
                }
                if (hasNonZeroBytes) break;
            }

            assertTrue(hasNonZeroBytes, "Salt should contain non-zero bytes");
        }

        @Test
        void shouldGenerateCryptographicallySecureRandomSalt() {
            byte[] salt1 = PasswordUtil.getSalt();
            byte[] salt2 = PasswordUtil.getSalt();

            assertFalse(Arrays.equals(salt1, salt2),
                "Consecutive salt generations should produce different results");
        }

        @Test
        void shouldReturnConsistentArrayLengthAcrossMultipleCalls() {
            for (int i = 0; i < 50; i++) {
                byte[] salt = PasswordUtil.getSalt();
                assertEquals(16, salt.length,
                    "Salt length should be consistent across all calls");
            }
        }

        @Test
        void shouldNotThrowAnyExceptions() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    PasswordUtil.getSalt();
                }
            });
        }
    }

    @Nested
    class HashPasswordTests {

        @Test
        void shouldHashPasswordSuccessfully() throws Exception {
            String password = "testPassword123";
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            String result = PasswordUtil.hashPassword(password, salt);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }

        @Test
        void shouldReturnDifferentHashesForSamePasswordWithDifferentSalt() throws Exception {
            String password = "testPassword123";
            byte[] salt1 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
            byte[] salt2 = new byte[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

            String hash1 = PasswordUtil.hashPassword(password, salt1);
            String hash2 = PasswordUtil.hashPassword(password, salt2);

            assertNotEquals(hash1, hash2);
        }

        @Test
        void shouldReturnSameHashForSamePasswordAndSalt() throws Exception {
            String password = "testPassword123";
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            String hash1 = PasswordUtil.hashPassword(password, salt);
            String hash2 = PasswordUtil.hashPassword(password, salt);

            assertEquals(hash1, hash2);
        }

        @Test
        void shouldHandleEmptyPassword() throws Exception {
            String password = "";
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            String result = PasswordUtil.hashPassword(password, salt);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }

        @Test
        void shouldHandleNullPassword() {
            String password = null;
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            assertThrows(NullPointerException.class, () -> {
                PasswordUtil.hashPassword(password, salt);
            });
        }

        @Test
        void shouldHandleEmptySalt() {
            String password = "testPassword123";
            byte[] salt = new byte[0];

            assertThrows(IllegalArgumentException.class, () -> {
                PasswordUtil.hashPassword(password, salt);
            });
        }

        @Test
        void shouldHandleNullSalt() {
            String password = "testPassword123";
            byte[] salt = null;

            assertThrows(NullPointerException.class, () -> {
                PasswordUtil.hashPassword(password, salt);
            });
        }

        @Test
        void shouldHandleSpecialCharactersInPassword() throws Exception {
            String password = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            String result = PasswordUtil.hashPassword(password, salt);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }

        @Test
        void shouldHandleVeryLongPassword() throws Exception {
            String password = "a".repeat(1000);
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            String result = PasswordUtil.hashPassword(password, salt);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }

        @Test
        void shouldReturnConsistentHashLength() throws Exception {
            String password = "testPassword123";
            byte[] salt = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

            String hash1 = PasswordUtil.hashPassword(password, salt);
            String hash2 = PasswordUtil.hashPassword("differentPassword", salt);

            assertEquals(hash1.length(), hash2.length());
        }
    }
}
