package com.studentresult.dao;

import com.studentresult.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for User/Admin authentication and management.
 */
public class UserDao {

    /**
     * Finds a user by their username (case-insensitive).
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, full_name, role, created_at FROM users WHERE username = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Creates a new user in the database.
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole() != null ? user.getRole() : "ADMIN");
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("salt"),
            rs.getString("full_name"),
            rs.getString("role"),
            rs.getString("created_at")
        );
    }
}

