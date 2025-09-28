package com.twitter.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Утилитный класс для работы с паролями пользователей.
 * Обеспечивает криптографически безопасное хеширование паролей
 * с использованием алгоритма PBKDF2 и генерацию случайных солей.
 * 
 * @author Twitter Team
 * @version 1.0
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    /**
     * Генерирует криптографически безопасную соль для хеширования пароля.
     * Использует SecureRandom для генерации 16-байтовой случайной соли.
     * Соль необходима для предотвращения атак по словарю и радужным таблицам.
     * 
     * @return массив байтов с криптографически безопасной солью
     */
    public static byte[] getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Хеширует пароль с использованием соли и алгоритма PBKDF2.
     * Использует PBKDF2WithHmacSHA256 с 10000 итераций и длиной ключа 256 бит.
     * Результат возвращается в Base64 кодировке для безопасного хранения.
     * 
     * @param password пароль в открытом виде для хеширования
     * @param salt соль для хеширования (16 байт)
     * @return хеш пароля в Base64 кодировке
     * @throws NoSuchAlgorithmException если алгоритм PBKDF2 недоступен
     * @throws InvalidKeySpecException если параметры ключа некорректны
     */
    public static String hashPassword(String password, byte[] salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}
