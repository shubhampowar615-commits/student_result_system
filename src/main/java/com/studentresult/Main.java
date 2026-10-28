package com.studentresult;

import com.studentresult.dao.DatabaseManager;
import com.studentresult.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Entry point for the Student Result Management System desktop application.
 * Initializes the SQLite database, configures the UI look-and-feel,
 * and launches the Administrator Login interface.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize SQLite Database schema and sample seed defaults
        DatabaseManager.initializeDatabase();

        // Configure Java Swing Look and Feel
        configureLookAndFeel();

        // Launch Application GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            // Prefer Nimbus or System Look and Feel for clean modern borders
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Defaulting to standard Swing Look & Feel: " + e.getMessage());
        }
    }
}

