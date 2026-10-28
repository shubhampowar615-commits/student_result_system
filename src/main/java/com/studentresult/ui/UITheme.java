package com.studentresult.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Styling and design system constants and factory methods for the Java Swing interface.
 * Implements the required navy blue, white, and light grey palette.
 */
public class UITheme {

    // Palette: Navy Blue, White, and Light Grey
    public static final Color NAVY_PRIMARY     = new Color(27, 54, 93);    // #1B365D - Deep College Navy
    public static final Color NAVY_DARK        = new Color(15, 33, 60);    // #0F213C - Header / Active Sidebar
    public static final Color NAVY_HOVER       = new Color(38, 72, 119);   // #264877 - Hover state
    public static final Color ACCENT_BLUE      = new Color(37, 99, 235);   // #2563EB - Highlights
    
    public static final Color BG_LIGHT         = new Color(245, 247, 250); // #F5F7FA - Light grey canvas
    public static final Color PANEL_WHITE      = Color.WHITE;              // #FFFFFF - White card backgrounds
    public static final Color BORDER_GREY      = new Color(220, 226, 235); // #DCE2EB - Crisp outline borders
    public static final Color BORDER_FOCUS     = new Color(37, 99, 235);
    
    public static final Color TEXT_PRIMARY     = new Color(30, 41, 59);    // #1E293B - High contrast dark slate
    public static final Color TEXT_MUTED       = new Color(100, 116, 139); // #64748B - Secondary labels
    public static final Color TEXT_LIGHT       = Color.WHITE;
    
    // Status badges
    public static final Color STATUS_PASS_BG   = new Color(220, 252, 231); // Light emerald
    public static final Color STATUS_PASS_TXT  = new Color(22, 101, 52);   // Deep green
    public static final Color STATUS_FAIL_BG   = new Color(254, 226, 226); // Light red
    public static final Color STATUS_FAIL_TXT  = new Color(153, 27, 27);   // Deep red
    public static final Color STATUS_INC_BG    = new Color(254, 243, 199); // Light amber
    public static final Color STATUS_INC_TXT   = new Color(180, 83, 9);    // Deep amber

    // Standard Typography
    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD         = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL        = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC_NUM   = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Creates a styled button matching the navy/white/grey aesthetic.
     */
    public static JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 24, 34));

        if (isPrimary) {
            button.setBackground(NAVY_PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(NAVY_PRIMARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GREY, 1),
                BorderFactory.createEmptyBorder(5, 13, 5, 13)
            ));
        }

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(NAVY_HOVER);
                } else {
                    button.setBackground(new Color(240, 244, 250));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(NAVY_PRIMARY);
                } else {
                    button.setBackground(Color.WHITE);
                }
            }
        });

        return button;
    }

    /**
     * Creates a standard styled text field with rounded padding and grey border.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    /**
     * Styles a JComboBox for clean consistency.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_REGULAR);
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 32));
        return combo;
    }

    /**
     * Creates a white card container with a crisp border and padding.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    /**
     * Creates a styled metric summary card for the Dashboard.
     */
    public static JPanel createMetricCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new java.awt.BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw colored top accent line
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 4);
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(12, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_MUTED);

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(FONT_METRIC_NUM);
        valLabel.setForeground(TEXT_PRIMARY);

        card.add(titleLabel, java.awt.BorderLayout.NORTH);
        card.add(valLabel, java.awt.BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(170, 85));
        return card;
    }

    /**
     * Styles a Swing JTable with Navy header, alternating rows, and proper padding.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(30);
        table.setGridColor(BORDER_GREY);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(224, 235, 250));
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(NAVY_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setReorderingAllowed(false);

        // Center align default headers
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Status column badge renderer
        StatusBadgeCellRenderer badgeRenderer = new StatusBadgeCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i).toLowerCase();
            if (colName.contains("status") || colName.contains("result")) {
                table.getColumnModel().getColumn(i).setCellRenderer(badgeRenderer);
            }
        }
    }

    /**
     * Custom cell renderer displaying PASS, FAIL, INCOMPLETE as colored pill badges.
     */
    public static class StatusBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);

            String text = value != null ? value.toString() : "";
            label.setText(text);
            label.setFont(FONT_BOLD);

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
                return label;
            }

            if ("PASS".equalsIgnoreCase(text)) {
                label.setBackground(STATUS_PASS_BG);
                label.setForeground(STATUS_PASS_TXT);
            } else if ("FAIL".equalsIgnoreCase(text)) {
                label.setBackground(STATUS_FAIL_BG);
                label.setForeground(STATUS_FAIL_TXT);
            } else if ("INCOMPLETE".equalsIgnoreCase(text) || "ABSENT".equalsIgnoreCase(text)) {
                label.setBackground(STATUS_INC_BG);
                label.setForeground(STATUS_INC_TXT);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(TEXT_PRIMARY);
            }
            return label;
        }
    }
}

