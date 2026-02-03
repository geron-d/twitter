package com.twitter.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utility class for secure password handling in Twitter user management.
 * <p>
 * This class provides cryptographically secure password hashing using the PBKDF2
 * algorithm with HMAC-SHA256. It generates secure random salts and implements
 * industry-standard security practices for password storage. The implementation
 * uses 10,000 iterations and 256-bit key length for optimal security.
 * <p>
 *
 * @author geron
 * @version 1.0
 */
public class PasswordUtil {

    /**
     * PBKDF2 algorithm with HMAC-SHA256 for password hashing.
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Number of iterations for PBKDF2 password hashing.
     * Higher values increase security but also computation time.
     * 10,000 iterations provide a good balance between security and performance.
     */
    private static final int ITERATIONS = 10000;

    /**
     * Key length in bits for PBKDF2 password hashing.
     * 256 bits provide strong cryptographic security for password storage.
     */
    private static final int KEY_LENGTH = 256;

    /**
     * Generates a cryptographically secure salt for password hashing.
     * <p>
     * This method creates a 16-byte random salt using SecureRandom, which is
     * essential for preventing dictionary attacks and rainbow table attacks.
     * Each salt should be unique for each password to ensure maximum security.
     *
     * @return 16-byte array containing cryptographically secure random salt
     */
    public static byte[] getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password using PBKDF2 with the provided salt.
     * <p>
     * This method implements PBKDF2WithHmacSHA256 with 10,000 iterations and
     * 256-bit key length. The resulting hash is encoded in Base64 for safe storage.
     * This approach provides strong protection against brute force and rainbow table attacks.
     *
     * @param password the plain text password to hash
     * @param salt     the salt to use for hashing (should be 16 bytes)
     * @return Base64-encoded hash of the password
     * @throws NoSuchAlgorithmException if PBKDF2 algorithm is not available
     * @throws InvalidKeySpecException  if key specification parameters are invalid
     */
    public static String hashPassword(String password, byte[] salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}