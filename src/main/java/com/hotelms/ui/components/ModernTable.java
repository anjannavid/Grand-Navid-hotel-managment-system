package com.hotelms.ui.components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernTable extends JTable {

    private int hoveredRow = -1;

    public ModernTable(DefaultTableModel model) {
        super(model);
        configure();
    }

    public ModernTable() {
        super();
        configure();
    }

    private void configure() {
        setBackground(ThemeManager.BG_PRIMARY);
        setForeground(ThemeManager.TEXT_PRIMARY);
        setFont(ThemeManager.FONT_BODY);
        setRowHeight(38);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(ThemeManager.ACCENT_PRIMARY);
        setSelectionForeground(ThemeManager.TEXT_INVERSE);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setFillsViewportHeight(true);
        getTableHeader().setReorderingAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom header
        JTableHeader header = getTableHeader();
        header.setBackground(ThemeManager.BG_TERTIARY);
        header.setForeground(ThemeManager.TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));

        // Header renderer with padding
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(ThemeManager.TEXT_SECONDARY);
                lbl.setBackground(ThemeManager.BG_TERTIARY);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER),
                    BorderFactory.createEmptyBorder(0, 12, 0, 8)));
                lbl.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        });

        // Hover tracking
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                repaint();
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        if (isRowSelected(row)) {
            c.setBackground(ThemeManager.ACCENT_PRIMARY);
            c.setForeground(ThemeManager.TEXT_INVERSE);
        } else if (row == hoveredRow) {
            c.setBackground(ThemeManager.BG_HOVER);
            c.setForeground(ThemeManager.TEXT_PRIMARY);
        } else if (row % 2 == 0) {
            c.setBackground(ThemeManager.BG_PRIMARY);
            c.setForeground(ThemeManager.TEXT_PRIMARY);
        } else {
            c.setBackground(ThemeManager.BG_SECONDARY);
            c.setForeground(ThemeManager.TEXT_PRIMARY);
        }

        // Add consistent cell padding with subtle bottom border
        if (c instanceof JLabel lbl) {
            lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 12, 0, 8)));
        }

        return c;
    }

    /** Wraps this table in a scroll pane with consistent styling */
    public JScrollPane inScrollPane() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true));
        sp.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        sp.setBackground(ThemeManager.BG_PRIMARY);
        return sp;
    }

    /** Sets a StatusBadge renderer on the specified column index */
    public void setStatusColumn(int col) {
        getColumnModel().getColumn(col).setCellRenderer(new StatusBadge());
    }

    /** Sets a currency renderer (right-aligned with padding) on the specified column index */
    public void setCurrencyColumn(int col) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 14));
                return this;
            }
        };
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        r.setFont(ThemeManager.FONT_BODY);
        getColumnModel().getColumn(col).setCellRenderer(r);
    }

    /** Sets a center-aligned renderer */
    public void setCenterColumn(int col) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        r.setHorizontalAlignment(SwingConstants.CENTER);
        getColumnModel().getColumn(col).setCellRenderer(r);
    }

    /** Fixes a column to a set pixel width */
    public void setColumnWidth(int col, int width) {
        TableColumn tc = getColumnModel().getColumn(col);
        tc.setPreferredWidth(width);
        tc.setMinWidth(width);
        tc.setMaxWidth(width);
    }
}
