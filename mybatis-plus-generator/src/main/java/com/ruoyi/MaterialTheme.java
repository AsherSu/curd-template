package com.ruoyi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * 轻量主题工具：统一颜色、间距、字体与基础组件样式
 * 目标风格：类 Android Material（明亮主题），加强留白和触达区域
 */
public final class MaterialTheme {
    private MaterialTheme() {}

    // 调色板（Light）
    public static final Color COLOR_PRIMARY = new Color(0x1E, 0x88, 0xE5);     // Blue 600
    public static final Color COLOR_ON_PRIMARY = Color.WHITE;
    public static final Color COLOR_SECONDARY = new Color(0x26, 0xA6, 0x9A);   // Teal 400
    public static final Color COLOR_BACKGROUND = new Color(0xFA, 0xFA, 0xFA); // 背景
    public static final Color COLOR_SURFACE = Color.WHITE;                     // 容器
    public static final Color COLOR_ON_SURFACE = new Color(0x21, 0x21, 0x21);  // 文本
    public static final Color COLOR_OUTLINE = new Color(0xE0, 0xE0, 0xE0);     // 轮廓线
    public static final Color COLOR_DIVIDER = new Color(0xDD, 0xDD, 0xDD);
    public static final Color COLOR_HOVER = new Color(0xF5, 0xF5, 0xF5);

    // 间距（8dp 网格）
    public static final int SPACE_1 = 4;
    public static final int SPACE_2 = 8;
    public static final int SPACE_3 = 12;
    public static final int SPACE_4 = 16;
    public static final int SPACE_6 = 24;

    // 字体
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 14);

    public static void applyGlobalUIFont() {
        setUIFont(new javax.swing.plaf.FontUIResource(FONT_BODY));
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static void styleRoot(Container root) {
        root.setBackground(COLOR_BACKGROUND);
    }

    public static void styleSurface(JComponent comp) {
        comp.setBackground(COLOR_SURFACE);
        comp.setForeground(COLOR_ON_SURFACE);
        comp.setBorder(new EmptyBorder(SPACE_4, SPACE_4, SPACE_4, SPACE_4));
    }

    public static void styleToolbar(JToolBar toolBar) {
        toolBar.setFloatable(false);
        toolBar.setBackground(COLOR_SURFACE);
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_OUTLINE));
    }

    public static void styleSplitPane(JSplitPane splitPane) {
        splitPane.setDividerSize(8);
        splitPane.setBackground(COLOR_SURFACE);
    }

    public static void styleTitledPanel(JPanel panel, String title) {
        panel.setBackground(COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, COLOR_OUTLINE),
                new EmptyBorder(SPACE_3, SPACE_3, SPACE_3, SPACE_3)
        ));
    }

    public static void styleButton(AbstractButton btn) {
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(COLOR_ON_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(SPACE_2, SPACE_6, SPACE_2, SPACE_6));
    }

    public static void styleSecondaryButton(AbstractButton btn) {
        btn.setBackground(COLOR_SECONDARY);
        btn.setForeground(COLOR_ON_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(SPACE_2, SPACE_6, SPACE_2, SPACE_6));
    }

    public static void styleList(JList<?> list) {
        list.setBackground(COLOR_SURFACE);
        list.setForeground(COLOR_ON_SURFACE);
        list.setFixedCellHeight(48);
        list.setSelectionBackground(new Color(COLOR_PRIMARY.getRed(), COLOR_PRIMARY.getGreen(), COLOR_PRIMARY.getBlue(), 32));
        list.setSelectionForeground(COLOR_ON_SURFACE);
        list.setBorder(new EmptyBorder(SPACE_2, SPACE_2, SPACE_2, SPACE_2));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_BACKGROUND);
        header.setForeground(COLOR_ON_SURFACE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_OUTLINE));
    }

    public static void styleNavPanel(JComponent navPanel) {
        navPanel.setBackground(COLOR_SURFACE);
        navPanel.setBorder(new MatteBorder(0, 0, 0, 1, COLOR_OUTLINE));
    }

    public static void styleLabelTitle(JLabel label) {
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_ON_SURFACE);
        label.setBorder(new EmptyBorder(SPACE_3, SPACE_2, SPACE_3, SPACE_2));
        label.setOpaque(true);
        label.setBackground(COLOR_SURFACE);
    }
} 