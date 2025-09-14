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
}
