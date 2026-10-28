package com.studentresult.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying secure password hashing, salt uniqueness, and verification logic.
 */
class PasswordHasherTest {

    @Test
    @DisplayName("Should generate a 32-character hexadecimal random salt")
    void testGenerateSalt() {
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();

        assertNotNull(salt1);
        assertEquals(32, salt1.length()); // 16 bytes = 32 hex chars
        assertNotEquals(salt1, salt2);     // Salts should be distinct
    }

    @Test
    @DisplayName("Should generate reproducible SHA-256 hash with identical password and salt")
    void testHashPasswordReproducibility() {
        String password = "adminPassword123!";
        String salt = PasswordHasher.generateSalt();

        String hash1 = PasswordHasher.hashPassword(password, salt);
        String hash2 = PasswordHasher.hashPassword(password, salt);

        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 produces 64 hex characters (32 bytes)
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should verify password correctly with matching credentials")
    void testVerifyPasswordSuccess() {
        String password = "SecretPassword";
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        assertTrue(PasswordHasher.verifyPassword(password, salt, hash));
    }

    @Test
    @DisplayName("Should reject verification when wrong password is supplied")
    void testVerifyPasswordFailure() {
        String password = "CorrectPassword";
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        assertFalse(PasswordHasher.verifyPassword("WrongPassword", salt, hash));
        assertFalse(PasswordHasher.verifyPassword("", salt, hash));
        assertFalse(PasswordHasher.verifyPassword(null, salt, hash));
    }
}

