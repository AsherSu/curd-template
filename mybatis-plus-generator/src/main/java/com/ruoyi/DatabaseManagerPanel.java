package com.ruoyi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class DatabaseManagerPanel extends JPanel {
    private final DefaultListModel<DbConnectionInfo> listModel = new DefaultListModel<>();
    private final JList<DbConnectionInfo> connList = new JList<DbConnectionInfo>(listModel) {
        @Override public String getToolTipText(java.awt.event.MouseEvent event) {
            DbConnectionInfo c = getSelectedValue();
            return c == null ? null : c.getUrl();
        }
    };

    private final JTextField nameField = new JTextField(24);
    private final JTextField urlField = new JTextField(32);
    private final JTextField userField = new JTextField(24);
    private final JPasswordField passField = new JPasswordField(24);
    private final JTextField driverField = new JTextField(32);

    private final JButton btnNew = new JButton("新建");
    private final JButton btnSave = new JButton("保存");
    private final JButton btnDelete = new JButton("删除");
    private final JButton btnTest = new JButton("测试连接");
    private final JButton btnSetActive = new JButton("设为活动");

    private final DefaultTableModel schemaModel = new DefaultTableModel(new Object[]{"Schema/DB"}, 0) { public boolean isCellEditable(int r,int c){return false;}};
    private final JTable schemaTable = new JTable(schemaModel);
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"表名"}, 0) { public boolean isCellEditable(int r,int c){return false;}};
    private final JTable tablesTable = new JTable(tableModel);

    public DatabaseManagerPanel() {
        setLayout(new BorderLayout());
        MaterialTheme.styleSurface(this);

        // 左侧连接列表
        connList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connList.setCellRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DbConnectionInfo) {
                    DbConnectionInfo c = (DbConnectionInfo) value;
                    lb.setText(c.getName());
                }
                return lb;
            }
        });
        MaterialTheme.styleList(connList);
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JScrollPane(connList), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(260, 0));
        MaterialTheme.styleNavPanel(left);

        // 右侧表单
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("连接信息"));
        form.setBackground(MaterialTheme.COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8); gbc.anchor = GridBagConstraints.WEST;
        int row = 0;
        gbc.gridx=0; gbc.gridy=row; form.add(new JLabel("名称"), gbc); gbc.gridx=1; form.add(nameField, gbc);
        row++; gbc.gridx=0; gbc.gridy=row; form.add(new JLabel("URL"), gbc); gbc.gridx=1; form.add(urlField, gbc);
        row++; gbc.gridx=0; gbc.gridy=row; form.add(new JLabel("用户名"), gbc); gbc.gridx=1; form.add(userField, gbc);
        row++; gbc.gridx=0; gbc.gridy=row; form.add(new JLabel("密码"), gbc); gbc.gridx=1; form.add(passField, gbc);
        row++; gbc.gridx=0; gbc.gridy=row; form.add(new JLabel("驱动类名"), gbc); gbc.gridx=1; form.add(driverField, gbc);

        JToolBar toolbar = new JToolBar();
        MaterialTheme.styleToolbar(toolbar);
        MaterialTheme.styleButton(btnNew);
        MaterialTheme.styleSecondaryButton(btnSave);
        MaterialTheme.styleSecondaryButton(btnDelete);
        MaterialTheme.styleSecondaryButton(btnTest);
        MaterialTheme.styleButton(btnSetActive);
        toolbar.add(btnNew);
        toolbar.add(btnSave);
        toolbar.add(btnDelete);
        toolbar.add(btnTest);
        toolbar.add(btnSetActive);

        JPanel rightTop = new JPanel(new BorderLayout());
        rightTop.add(toolbar, BorderLayout.NORTH);
        rightTop.add(form, BorderLayout.CENTER);

        JPanel rightBottom = new JPanel(new GridLayout(1,2));
        JPanel schemaPanel = new JPanel(new BorderLayout());
        schemaPanel.setBorder(BorderFactory.createTitledBorder("Schemas"));
        schemaPanel.add(new JScrollPane(schemaTable), BorderLayout.CENTER);
        JPanel tablesPanel = new JPanel(new BorderLayout());
        tablesPanel.setBorder(BorderFactory.createTitledBorder("Tables"));
        tablesPanel.add(new JScrollPane(tablesTable), BorderLayout.CENTER);
        rightBottom.add(schemaPanel);
        rightBottom.add(tablesPanel);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTop, rightBottom);
        rightSplit.setResizeWeight(0.5);
        rightSplit.setContinuousLayout(true);
        MaterialTheme.styleSplitPane(rightSplit);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, rightSplit);
        split.setResizeWeight(0.28);
        split.setContinuousLayout(true);
        MaterialTheme.styleSplitPane(split);

        add(split, BorderLayout.CENTER);

        // 事件
        btnNew.addActionListener(this::onNew);
        btnSave.addActionListener(this::onSave);
        btnDelete.addActionListener(this::onDelete);
        btnTest.addActionListener(this::onTest);
        btnSetActive.addActionListener(this::onSetActive);
        connList.addListSelectionListener(e -> { if(!e.getValueIsAdjusting()) fillForm(connList.getSelectedValue()); });
        schemaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadTables();
        });

        reloadConnections();
        reloadSchemas();
    }

    private void onNew(ActionEvent e) {
        nameField.setText("");
        urlField.setText("");
        userField.setText("");
        passField.setText("");
        driverField.setText("com.mysql.cj.jdbc.Driver");
        connList.clearSelection();
    }

    private void onSave(ActionEvent e) {
        DbConnectionInfo sel = connList.getSelectedValue();
        if (sel == null) sel = new DbConnectionInfo();
        sel.setName(nameField.getText().trim());
        sel.setUrl(urlField.getText().trim());
        sel.setUsername(userField.getText().trim());
        sel.setPassword(new String(passField.getPassword()));
        sel.setDriverClassName(driverField.getText().trim());
        ConnectionRepository.save(sel);
        reloadConnections();
    }

    private void onDelete(ActionEvent e) {
        DbConnectionInfo sel = connList.getSelectedValue();
        if (sel == null) return;
        int opt = JOptionPane.showConfirmDialog(this, "确认删除所选连接?", "确认", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            ConnectionRepository.delete(sel.getId());
            reloadConnections();
        }
    }

    private void onTest(ActionEvent e) {
        DbConnectionInfo tmp = new DbConnectionInfo();
        tmp.setName(nameField.getText().trim());
        tmp.setUrl(urlField.getText().trim());
        tmp.setUsername(userField.getText().trim());
        tmp.setPassword(new String(passField.getPassword()));
        tmp.setDriverClassName(driverField.getText().trim());
        boolean ok = DatabaseService.test(tmp);
        JOptionPane.showMessageDialog(this, ok ? "连接成功" : "连接失败", ok ? "成功" : "失败", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void onSetActive(ActionEvent e) {
        DbConnectionInfo sel = connList.getSelectedValue();
        if (sel == null) return;
        ConnectionRepository.setActive(sel.getId());
        JOptionPane.showMessageDialog(this, "已设为活动连接: " + sel.getName());
        reloadSchemas();
    }

    private void reloadConnections() {
        listModel.clear();
        List<DbConnectionInfo> list = ConnectionRepository.findAll();
        for (DbConnectionInfo c : list) listModel.addElement(c);
    }

    private void fillForm(DbConnectionInfo c) {
        if (c == null) return;
        nameField.setText(c.getName());
        urlField.setText(c.getUrl());
        userField.setText(c.getUsername());
        passField.setText(c.getPassword());
        driverField.setText(c.getDriverClassName());
    }

    private void reloadSchemas() {
        schemaModel.setRowCount(0);
        for (String s : DatabaseService.listSchemas()) schemaModel.addRow(new Object[]{s});
    }

    private void loadTables() {
        int row = schemaTable.getSelectedRow();
        String schema = row >= 0 ? (String) schemaModel.getValueAt(row, 0) : null;
        tableModel.setRowCount(0);
        if (schema != null) {
            for (String t : DatabaseService.listTables(schema)) tableModel.addRow(new Object[]{t});
        }
    }
} 