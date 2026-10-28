package com.studentresult.service;

import com.studentresult.dao.UserDao;
import com.studentresult.model.User;
import com.studentresult.util.PasswordHasher;
import java.sql.SQLException;

/**
 * Service handling user authentication and credential security.
 */
public class AuthService {

    private final UserDao userDao;
    private static User currentUser;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates an administrator using their username and password.
     *
     * @param username Provided username
     * @param password Provided plaintext password
     * @return Logged-in User if successful, null if failed
     */
    public User login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null) {
            return null;
        }

        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            return null;
        }

        boolean valid = PasswordHasher.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (valid) {
            currentUser = user;
            return user;
        }
        return null;
    }

    /**
     * Clears current session upon logout.
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Gets currently authenticated user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets currently authenticated user (for testing or session restore).
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}

