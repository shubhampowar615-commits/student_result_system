package com.studentresult.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for secure password hashing using SHA-256 and unique cryptographic salt.
 * Ensures passwords are not stored in plaintext in the SQLite database.
 */
public class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new random 16-byte hexadecimal salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Computes the SHA-256 hash of a password combined with a salt.
     *
     * @param password Plaintext password
     * @param salt Hexadecimal salt
     * @return Hexadecimal SHA-256 hash string
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Hash password combined with salt
            String combined = salt + password;
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies whether a given plaintext password matches the stored salt and hash.
     *
     * @param plainPassword Plaintext candidate password
     * @param salt Stored hex salt
     * @param expectedHash Stored hex hash
     * @return true if matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(plainPassword, salt);
        return calculated.equalsIgnoreCase(expectedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

