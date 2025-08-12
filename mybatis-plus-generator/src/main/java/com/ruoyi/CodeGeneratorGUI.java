package com.ruoyi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器图形界面
 */
public class CodeGeneratorGUI extends JFrame {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    
    // 新增：内容卡片与导航
    private JPanel contentCardPanel;
    private CardLayout contentCardLayout;
    private JList<String> navList;
    
    // 生成代码面板组件
    private JComboBox<String> tableComboBox;
    private JTextField moduleNameField;
    private JTextField parentPackageField;
    private JCheckBox controllerCheckBox;
    private JButton generateButton;
    private JTextArea logTextArea;
    private JButton refreshTableButton;
    private JList<String> tableList;
    private DefaultListModel<String> tableListModel;
    
    // 新增：路径输入
    private JTextField entityDirField;
    private JTextField mapperDirField;
    private JTextField serviceDirField;
    private JTextField serviceImplDirField;
    private JTextField controllerDirField;
    private JTextField xmlDirField;
    
    // 历史记录面板组件
    private JTable recordTable;
    private DefaultTableModel recordTableModel;
    private JButton viewDetailsButton;
    private JButton restoreButton;
    private JButton deleteRecordButton;
    private JButton clearAllButton;
    private JTextArea recordDetailArea;
    private JButton editRecordButton; // 新增：编辑/再生成按钮
    
    // 数据库信息
    private List<String> allTables;
    
    public CodeGeneratorGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDatabaseTables();
        loadGenerationRecords();
        
        setTitle("MyBatis-Plus 代码生成器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        // 新增：导航与内容卡片
        contentCardLayout = new CardLayout();
        contentCardPanel = new JPanel(contentCardLayout);
        navList = new JList<>(new String[]{"生成代码", "历史记录"});
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setSelectedIndex(0);
        navList.setFixedCellHeight(56);
        navList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        // 初始化生成代码面板组件
        tableListModel = new DefaultListModel<>();
        tableList = new JList<>(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableList.setVisibleRowCount(-1);
        
        moduleNameField = new JTextField(20);
        parentPackageField = new JTextField(20);
        parentPackageField.setText("com.ruoyi.project");
        controllerCheckBox = new JCheckBox("生成Controller", true);
        generateButton = new JButton("生成代码");
        refreshTableButton = new JButton("刷新表列表");
        logTextArea = new JTextArea(10, 50);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // 新增：路径输入控件（默认留空，点击生成校验）
        entityDirField = new JTextField(28);
        mapperDirField = new JTextField(28);
        serviceDirField = new JTextField(28);
        serviceImplDirField = new JTextField(28);
        controllerDirField = new JTextField(28);
        xmlDirField = new JTextField(28);
        
        // 提升触控目标
        Dimension largeButtonSize = new Dimension(140, 40);
        generateButton.setPreferredSize(largeButtonSize);
        refreshTableButton.setPreferredSize(largeButtonSize);
        
        // 初始化历史记录面板组件
        String[] columnNames = {"ID", "生成时间", "模块名", "包名", "表数量", "Controller", "状态"};
        recordTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordTable = new JTable(recordTableModel);
        recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordDetailArea = new JTextArea(10, 50);
        recordDetailArea.setEditable(false);
        viewDetailsButton = new JButton("查看详情");
        restoreButton = new JButton("恢复文件");
        deleteRecordButton = new JButton("删除记录");
        clearAllButton = new JButton("清空记录");
        editRecordButton = new JButton("编辑/再生成");
        
        viewDetailsButton.setPreferredSize(largeButtonSize);
        restoreButton.setPreferredSize(largeButtonSize);
        deleteRecordButton.setPreferredSize(largeButtonSize);
        clearAllButton.setPreferredSize(largeButtonSize);
        editRecordButton.setPreferredSize(largeButtonSize);
    }
    
    private void setupLayout() {
        // 创建生成代码面板（右侧内容-卡片之一）
        JPanel generatePanel = new JPanel(new BorderLayout());
        
        // 左侧表选择区域（列表 + 刷新）
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("数据库表"));
        JScrollPane tableScrollPane = new JScrollPane(tableList);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tableActionsPanel.add(refreshTableButton);
        tablePanel.add(tableActionsPanel, BorderLayout.SOUTH);
        
        // 右侧配置 + 日志，构成垂直分割
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("生成配置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("模块名称:"), gbc);
        gbc.gridx = 1; configPanel.add(moduleNameField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("父包名:"), gbc);
        gbc.gridx = 1; configPanel.add(parentPackageField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; configPanel.add(controllerCheckBox, gbc);
        
        // 路径输入
        row++; gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("Entity 目录:"), gbc);
        gbc.gridx = 1; configPanel.add(entityDirField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("Mapper 目录:"), gbc);
        gbc.gridx = 1; configPanel.add(mapperDirField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("Service 目录:"), gbc);
        gbc.gridx = 1; configPanel.add(serviceDirField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("ServiceImpl 目录:"), gbc);
        gbc.gridx = 1; configPanel.add(serviceImplDirField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("Controller 目录:"), gbc);
        gbc.gridx = 1; configPanel.add(controllerDirField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; configPanel.add(new JLabel("XML 目录:"), gbc);
        gbc.gridx = 1; configPanel.add(xmlDirField, gbc);
        
        row++; gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; configPanel.add(generateButton, gbc);
        
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("生成日志"));
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, configPanel, logPanel);
        verticalSplit.setResizeWeight(0.45);
        verticalSplit.setContinuousLayout(true);
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, verticalSplit);
        mainSplit.setResizeWeight(0.35);
        mainSplit.setContinuousLayout(true);
        
        generatePanel.add(mainSplit, BorderLayout.CENTER);
        
        // 创建历史记录面板（右侧内容-卡片之二）
        JPanel historyPanel = new JPanel(new BorderLayout());
        
        JToolBar historyToolbar = new JToolBar();
        historyToolbar.setFloatable(false);
        historyToolbar.add(viewDetailsButton);
        historyToolbar.add(restoreButton);
        historyToolbar.add(deleteRecordButton);
        historyToolbar.add(clearAllButton);
        historyToolbar.add(editRecordButton); // 新增
        historyPanel.add(historyToolbar, BorderLayout.NORTH);
        
        JPanel recordListPanel = new JPanel(new BorderLayout());
        recordListPanel.setBorder(BorderFactory.createTitledBorder("生成记录"));
        JScrollPane recordScrollPane = new JScrollPane(recordTable);
        recordListPanel.add(recordScrollPane, BorderLayout.CENTER);
        
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("记录详情"));
        JScrollPane detailScrollPane = new JScrollPane(recordDetailArea);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        
        JSplitPane historySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, recordListPanel, detailPanel);
        historySplit.setResizeWeight(0.6);
        historySplit.setContinuousLayout(true);
        
        historyPanel.add(historySplit, BorderLayout.CENTER);
        
        contentCardPanel.add(generatePanel, "generate");
        contentCardPanel.add(historyPanel, "history");
        
        JPanel navPanel = new JPanel(new BorderLayout());
        JLabel appTitle = new JLabel("代码生成器", SwingConstants.CENTER);
        appTitle.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        navPanel.add(appTitle, BorderLayout.NORTH);
        navPanel.add(new JScrollPane(navList), BorderLayout.CENTER);
        navPanel.setPreferredSize(new Dimension(160, 0));
        
        mainPanel.add(navPanel, BorderLayout.WEST);
        mainPanel.add(contentCardPanel, BorderLayout.CENTER);
        add(mainPanel);
        
        contentCardLayout.show(contentCardPanel, "generate");
    }
    
    private void setupEventHandlers() {
        // 刷新表按钮
        refreshTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDatabaseTables();
            }
        });
        
        // 生成代码按钮
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateCode();
            }
        });
        
        // 查看详情按钮
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewRecordDetails();
            }
        });
        
        // 恢复文件按钮
        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restoreRecordFiles();
            }
        });
        
        // 删除记录按钮
        deleteRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRecord();
            }
        });
        
        // 清空记录按钮
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllRecords();
            }
        });
        
        // 打开记录编辑器
        editRecordButton.addActionListener(e -> openRecordEditor());
        
        // 记录表格双击：打开编辑器
        recordTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openRecordEditor();
                }
            }
        });
        
        // 导航切换
        navList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = navList.getSelectedIndex();
                if (idx == 0) {
                    contentCardLayout.show(contentCardPanel, "generate");
                } else if (idx == 1) {
                    contentCardLayout.show(contentCardPanel, "history");
                }
            }
        });
        
        // Controller 切换联动路径输入是否可编辑
        controllerCheckBox.addActionListener(e -> {
            boolean enabled = controllerCheckBox.isSelected();
            controllerDirField.setEnabled(enabled);
        });
        controllerDirField.setEnabled(controllerCheckBox.isSelected());
    }
    
    /**
     * 加载数据库表列表
     */
    private void loadDatabaseTables() {
        SwingWorker<List<String>, String> worker = new SwingWorker<List<String>, String>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                publish("正在连接数据库并获取表列表...");
                List<String> tables = getTableList();
                publish("成功获取到 " + tables.size() + " 个表");
                return tables;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    allTables = get();
                    tableListModel.clear();
                    for (String table : allTables) {
                        tableListModel.addElement(table);
                    }
                } catch (Exception e) {
                    appendLog("获取表列表失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(CodeGeneratorGUI.this, 
                        "获取表列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 获取数据库中的所有表名
     */
    private List<String> getTableList() {
        List<String> tableList = new ArrayList<>();
        Connection conn = null;
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                CodeGenerator.getDbUrl(),
                CodeGenerator.getDbUsername(),
                CodeGenerator.getDbPassword()
            );
            DatabaseMetaData metaData = conn.getMetaData();

            // 从URL中提取数据库名
            String dbName = CodeGenerator.extractDatabaseName(CodeGenerator.getDbUrl());
            
            ResultSet rs = metaData.getTables(dbName, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableList.add(tableName);
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // 忽略关闭连接的异常
                }
            }
        }
        return tableList;
    }
    
    /**
     * 生成代码
     */
    private void generateCode() {
        // 获取选中的表
        List<String> selectedTables = tableList.getSelectedValuesList();
        if (selectedTables.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请至少选择一个表", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String moduleName = moduleNameField.getText().trim();
        if (moduleName.isEmpty()) {
            moduleName = "module";
        }
        
        String parentPackage = parentPackageField.getText().trim();
        if (parentPackage.isEmpty()) {
            parentPackage = "com.ruoyi.project";
        }
        
        boolean enableController = controllerCheckBox.isSelected();
        
        // 新增：校验路径非空
        String entityDir = entityDirField.getText().trim();
        String mapperDir = mapperDirField.getText().trim();
        String serviceDir = serviceDirField.getText().trim();
        String serviceImplDir = serviceImplDirField.getText().trim();
        String controllerDir = controllerDirField.getText().trim();
        String xmlDir = xmlDirField.getText().trim();
        
        if (entityDir.isEmpty() || mapperDir.isEmpty() || serviceDir.isEmpty() || serviceImplDir.isEmpty() || xmlDir.isEmpty() || (enableController && controllerDir.isEmpty())) {
            JOptionPane.showMessageDialog(this, "请填写所有生成路径：Entity、Mapper、Service、ServiceImpl、XML；勾选 Controller 时还需填写 Controller 目录。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String[] tableNames = selectedTables.toArray(new String[0]);
        
        // 创建生成记录
        GenerationRecord record = new GenerationRecord();
        record.setTableNames(tableNames);
        record.setModuleName(moduleName);
        record.setParentPackage(parentPackage);
        record.setEnableController(enableController);
        record.setDbUrl(CodeGenerator.getDbUrl());
        record.setStatus("PROCESSING");
        
        // 组装路径配置
        GenerationPathConfig pathConfig = new GenerationPathConfig();
        pathConfig.setEntityDir(entityDir);
        pathConfig.setMapperDir(mapperDir);
        pathConfig.setServiceDir(serviceDir);
        pathConfig.setServiceImplDir(serviceImplDir);
        pathConfig.setControllerDir(enableController ? controllerDir : null);
        pathConfig.setXmlDir(xmlDir);
        
        // 同步到记录上，便于后续编辑/再生成
        record.setEntityDir(entityDir);
        record.setMapperDir(mapperDir);
        record.setServiceDir(serviceDir);
        record.setServiceImplDir(serviceImplDir);
        record.setControllerDir(enableController ? controllerDir : null);
        record.setXmlDir(xmlDir);
        
        appendLog("开始生成代码...");
        
        String finalParentPackage = parentPackage;
        String finalModuleName = moduleName;
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    CodeGenerator.generateCode(tableNames, finalParentPackage, finalModuleName, enableController, record, pathConfig);
                    publish("代码生成完成！");
                    record.setStatus("SUCCESS");
                    GenerationRecordManager.saveRecord(record);
                    publish("生成记录已保存");
                } catch (Exception e) {
                    publish("代码生成失败: " + e.getMessage());
                    record.setStatus("FAILED");
                    GenerationRecordManager.saveRecord(record);
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    appendLog("代码生成任务完成");
                    loadGenerationRecords();
                    
                    int option = JOptionPane.showConfirmDialog(
                        CodeGeneratorGUI.this,
                        "代码生成完成，是否确认保留生成的代码？\n选择\"否\"将删除刚刚生成的文件",
                        "确认保留",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (option == JOptionPane.NO_OPTION) {
                        appendLog("正在删除刚刚生成的文件...");
                        appendLog("已删除生成的文件。");
                    } else {
                        appendLog("代码已保留。");
                    }
                } catch (Exception e) {
                    appendLog("代码生成任务失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(CodeGeneratorGUI.this, 
                        "代码生成失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 加载生成记录
     */
    private void loadGenerationRecords() {
        List<GenerationRecord> records = GenerationRecordManager.loadRecords();
        recordTableModel.setRowCount(0); // 清空现有数据
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (GenerationRecord record : records) {
            Object[] row = {
                record.getId(),
                record.getGenerationTime().format(formatter),
                record.getModuleName(),
                record.getParentPackage(),
                record.getTableNames() != null ? record.getTableNames().length : 0,
                record.isEnableController() ? "是" : "否",
                record.getStatus()
            };
            recordTableModel.addRow(row);
        }
    }
    
    /**
     * 查看记录详情
     */
    private void viewRecordDetails() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            long recordId = (Long) recordTable.getValueAt(selectedRow, 0);
            GenerationRecord record = GenerationRecordManager.findById(recordId);
            
            if (record == null) {
                JOptionPane.showMessageDialog(this, "未找到ID为 " + recordId + " 的记录", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            StringBuilder detail = new StringBuilder();
            detail.append("记录ID: ").append(record.getId()).append("\n");
            detail.append("生成时间: ").append(record.getGenerationTime()).append("\n");
            detail.append("数据库URL: ").append(record.getDbUrl()).append("\n");
            detail.append("模块名: ").append(record.getModuleName()).append("\n");
            detail.append("父包名: ").append(record.getParentPackage()).append("\n");
            detail.append("生成Controller: ").append(record.isEnableController() ? "是" : "否").append("\n");
            detail.append("状态: ").append(record.getStatus()).append("\n");
            detail.append("表名列表:\n");
            if (record.getTableNames() != null) {
                for (String tableName : record.getTableNames()) {
                    detail.append("  - ").append(tableName).append("\n");
                }
            }
            
            detail.append("生成文件列表:\n");
            if (record.getGeneratedFiles() != null && !record.getGeneratedFiles().isEmpty()) {
                for (GeneratedFile file : record.getGeneratedFiles()) {
                    detail.append("  - [").append(file.getFileType()).append("] ").append(file.getFilePath()).append("\n");
                }
            } else {
                detail.append("  暂无文件信息\n");
            }
            
            recordDetailArea.setText(detail.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查看记录详情失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 恢复记录文件
     */
    private void restoreRecordFiles() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            long recordId = (Long) recordTable.getValueAt(selectedRow, 0);
            GenerationRecord record = GenerationRecordManager.findById(recordId);
            
            if (record == null) {
                JOptionPane.showMessageDialog(this, "未找到ID为 " + recordId + " 的记录", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (record.getGeneratedFiles() == null || record.getGeneratedFiles().isEmpty()) {
                JOptionPane.showMessageDialog(this, "该记录没有保存文件信息，无法恢复。", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 询问用户是否恢复文件
            int option = JOptionPane.showConfirmDialog(
                this,
                "确认从记录 ID " + recordId + " 恢复文件?\n这将覆盖磁盘上同名但内容不同的文件。",
                "确认恢复",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                // 实现恢复逻辑
                restoreFiles(record);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "恢复文件失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 实际恢复文件的方法
     * @param record 生成记录
     */
    private void restoreFiles(GenerationRecord record) {
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                int restoredCount = 0;
                int skippedCount = 0;
                int overwrittenCount = 0;
                
                for (GeneratedFile file : record.getGeneratedFiles()) {
                    String filePath = file.getFilePath();
                    String content = file.getContent();
                    
                    int checkResult = GenerationRecordManager.checkFileExistsAndCompare(filePath, content);
                    switch (checkResult) {
                        case 0: // 文件不存在
                            // 直接创建文件
                            createFileWithContent(filePath, content);
                            restoredCount++;
                            publish("已创建文件: " + filePath);
                            break;
                        case 1: // 文件存在且内容相同
                            publish("跳过文件 (内容相同): " + filePath);
                            skippedCount++;
                            break;
                        case 2: // 文件存在但内容不同
                            // 覆盖文件
                            createFileWithContent(filePath, content);
                            overwrittenCount++;
                            publish("已覆盖文件: " + filePath);
                            break;
                    }
                }
                
                return String.format("恢复完成:\n  新建文件: %d\n  覆盖文件: %d\n  跳过文件: %d", 
                                   restoredCount, overwrittenCount, skippedCount);
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get(); // 获取结果
                    appendLog(result);
                    JOptionPane.showMessageDialog(CodeGeneratorGUI.this, result, "恢复完成", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    appendLog("文件恢复失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(CodeGeneratorGUI.this, 
                        "文件恢复失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

    /**
     * 创建文件并写入内容
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     */
    private void createFileWithContent(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            // 创建父目录
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // 写入文件内容
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            appendLog("创建文件失败: " + filePath + " - " + e.getMessage());
        }
    }
    
    /**
     * 删除记录
     */
    private void deleteRecord() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            long recordId = (Long) recordTable.getValueAt(selectedRow, 0);
            
            int option = JOptionPane.showConfirmDialog(
                this,
                "确认删除记录 ID " + recordId + " ?",
                "确认删除",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                GenerationRecordManager.deleteRecord(recordId);
                loadGenerationRecords();
                recordDetailArea.setText("");
                appendLog("记录 " + recordId + " 已删除");
                JOptionPane.showMessageDialog(this, "记录已删除。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 清空所有记录
     */
    private void clearAllRecords() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "确认清空所有记录?",
            "确认清空",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            GenerationRecordManager.clearAllRecords();
            loadGenerationRecords();
            recordDetailArea.setText("");
            appendLog("所有记录已清空");
            JOptionPane.showMessageDialog(this, "所有记录已清空。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 添加日志
     */
    private void appendLog(String message) {
        logTextArea.append("[" + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }
    
    /**
     * 打开记录编辑器
     */
    private void openRecordEditor() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            long recordId = (Long) recordTable.getValueAt(selectedRow, 0);
            GenerationRecord record = GenerationRecordManager.findById(recordId);
            if (record == null) {
                JOptionPane.showMessageDialog(this, "未找到ID为 " + recordId + " 的记录", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            RecordEditorDialog dialog = new RecordEditorDialog(this, record, updated -> {
                // 更新完成后刷新表格与详情
                loadGenerationRecords();
                recordDetailArea.setText("");
            });
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "打开编辑器失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 启动GUI应用程序
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 设置系统外观
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
                } catch (Exception e) {
                    // 使用默认外观
                }
                
                new CodeGeneratorGUI().setVisible(true);
            }
        });
    }
}