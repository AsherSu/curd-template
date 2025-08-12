package com.ruoyi;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordEditorDialog extends JDialog {
    private final GenerationRecord record;
    private final Consumer<GenerationRecord> onSaved;

    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JTextPane codePane;
    private JScrollPane codeScroll;
    private JButton saveToRecordButton;
    private JButton writeToDiskButton;
    private JButton writeAllToDiskButton;
    private JButton regenerateButton;
    private JLabel statusLabel;

    // 高亮相关
    private StyleContext styleContext;
    private Style styleDefault;
    private Style styleKeyword;
    private Style styleString;
    private Style styleComment;
    private Style styleAnnotation;
    private Timer highlightTimer;

    private static final String[] JAVA_KEYWORDS = new String[] {
            "abstract","assert","boolean","break","byte","case","catch","char","class","const","continue",
            "default","do","double","else","enum","extends","final","finally","float","for","goto","if",
            "implements","import","instanceof","int","interface","long","native","new","package","private",
            "protected","public","return","short","static","strictfp","super","switch","synchronized","this",
            "throw","throws","transient","try","void","volatile","while","var","record","sealed","permits","non-sealed"
    };

    public RecordEditorDialog(Frame owner, GenerationRecord record, Consumer<GenerationRecord> onSaved) {
        super(owner, "编辑记录 #" + record.getId(), true);
        this.record = record;
        this.onSaved = onSaved;

        initComponents();
        setupLayout();
        setupEvents();
        loadFiles();

        setSize(1100, 720);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        codePane = new JTextPane();
        codePane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        codeScroll = new JScrollPane(codePane);

        saveToRecordButton = new JButton("保存到记录");
        writeToDiskButton = new JButton("写入当前文件到磁盘");
        writeAllToDiskButton = new JButton("写入全部到磁盘");
        regenerateButton = new JButton("重新生成");
        statusLabel = new JLabel(" ");

        // 初始化样式
        styleContext = new StyleContext();
        styleDefault = styleContext.addStyle("default", null);
        StyleConstants.setForeground(styleDefault, new Color(220, 220, 220));
        StyleConstants.setFontFamily(styleDefault, Font.MONOSPACED);

        styleKeyword = styleContext.addStyle("keyword", null);
        StyleConstants.setForeground(styleKeyword, new Color(86, 156, 214));
        StyleConstants.setBold(styleKeyword, true);

        styleString = styleContext.addStyle("string", null);
        StyleConstants.setForeground(styleString, new Color(206, 145, 120));

        styleComment = styleContext.addStyle("comment", null);
        StyleConstants.setForeground(styleComment, new Color(106, 153, 85));
        StyleConstants.setItalic(styleComment, true);

        styleAnnotation = styleContext.addStyle("annotation", null);
        StyleConstants.setForeground(styleAnnotation, new Color(197, 134, 192));

        // 高亮防抖定时器
        highlightTimer = new Timer(250, e -> applyHighlighting());
        highlightTimer.setRepeats(false);
    }

    private void setupLayout() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("文件列表"));
        leftPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("代码编辑"));
        rightPanel.add(codeScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.33);
        splitPane.setContinuousLayout(true);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(saveToRecordButton);
        toolbar.add(writeToDiskButton);
        toolbar.add(writeAllToDiskButton);
        toolbar.add(regenerateButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusLabel, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        // 暗色背景更清晰
        codePane.setBackground(new Color(30, 30, 30));
        codePane.setForeground(new Color(220, 220, 220));
    }

    private void setupEvents() {
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedFileContent();
            }
        });

        saveToRecordButton.addActionListener(e -> saveCurrentToRecord());
        writeToDiskButton.addActionListener(e -> writeCurrentToDisk());
        writeAllToDiskButton.addActionListener(e -> writeAllToDisk());
        regenerateButton.addActionListener(e -> regenerate());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onSaved != null) onSaved.accept(record);
            }
        });

        codePane.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { scheduleHighlight(); }
            @Override public void removeUpdate(DocumentEvent e) { scheduleHighlight(); }
            @Override public void changedUpdate(DocumentEvent e) { scheduleHighlight(); }
        });
    }

    private void scheduleHighlight() {
        if (highlightTimer.isRunning()) highlightTimer.restart();
        else highlightTimer.start();
    }

    private void loadFiles() {
        fileListModel.clear();
        List<GeneratedFile> files = record.getGeneratedFiles();
        if (files != null) {
            for (GeneratedFile f : files) {
                fileListModel.addElement(f.getFilePath());
            }
        }
        if (!fileListModel.isEmpty()) {
            fileList.setSelectedIndex(0);
        }
    }

    private void loadSelectedFileContent() {
        int idx = fileList.getSelectedIndex();
        if (idx < 0) {
            codePane.setText("");
            return;
        }
        GeneratedFile gf = record.getGeneratedFiles().get(idx);
        codePane.setText(gf.getContent() != null ? gf.getContent() : "");
        codePane.setCaretPosition(0);
        applyHighlighting();
        status("已载入: " + gf.getFilePath());
    }

    private void saveCurrentToRecord() {
        int idx = fileList.getSelectedIndex();
        if (idx < 0) return;
        GeneratedFile gf = record.getGeneratedFiles().get(idx);
        gf.setContent(codePane.getText());
        GenerationRecordManager.updateRecord(record);
        status("已保存至记录: " + gf.getFilePath());
    }

    private void writeCurrentToDisk() {
        int idx = fileList.getSelectedIndex();
        if (idx < 0) return;
        GeneratedFile gf = record.getGeneratedFiles().get(idx);
        writeFile(gf.getFilePath(), codePane.getText());
        status("已写入磁盘: " + gf.getFilePath());
    }

    private void writeAllToDisk() {
        if (record.getGeneratedFiles() == null) return;
        for (GeneratedFile gf : record.getGeneratedFiles()) {
            writeFile(gf.getFilePath(), gf.getContent());
        }
        status("全部文件已写入磁盘");
    }

    private void regenerate() {
        if (record.getTableNames() == null || record.getTableNames().length == 0) {
            JOptionPane.showMessageDialog(this, "记录中无表信息，无法重新生成", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        GenerationPathConfig pathConfig = new GenerationPathConfig(
                record.getEntityDir(),
                record.getMapperDir(),
                record.getServiceDir(),
                record.getServiceImplDir(),
                record.isEnableController() ? record.getControllerDir() : null,
                record.getXmlDir()
        );

        status("正在重新生成...");
        setButtonsEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                CodeGenerator.generateCode(
                        record.getTableNames(),
                        record.getParentPackage(),
                        record.getModuleName(),
                        record.isEnableController(),
                        record,
                        pathConfig
                );
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    GenerationRecordManager.updateRecord(record);
                    loadFiles();
                    status("重新生成完成");
                    if (onSaved != null) onSaved.accept(record);
                } catch (Exception e) {
                    status("重新生成失败: " + e.getMessage());
                    JOptionPane.showMessageDialog(RecordEditorDialog.this, "重新生成失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void writeFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(path, content == null ? new byte[0] : content.getBytes());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "写入文件失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        saveToRecordButton.setEnabled(enabled);
        writeToDiskButton.setEnabled(enabled);
        writeAllToDiskButton.setEnabled(enabled);
        regenerateButton.setEnabled(enabled);
        fileList.setEnabled(enabled);
        codePane.setEditable(enabled);
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    // 简易 Java 语法高亮
    private void applyHighlighting() {
        StyledDocument doc = codePane.getStyledDocument();
        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return;
        }
        // 清空为默认样式
        doc.setCharacterAttributes(0, text.length(), styleDefault, true);

        // 顺序：注释块、多行注释、单行注释、字符串、注解、关键字
        // 1) 多行注释 /* ... */
        applyPattern(doc, text, "/\\*.*?\\*/", Pattern.DOTALL, styleComment);
        // 2) 单行注释 // ... (多行开启模式)
        applyPattern(doc, text, "//.*$", Pattern.MULTILINE, styleComment);
        // 3) 字符串与字符常量
        applyPattern(doc, text, "\"([^\\\"\\r\\n]|\\\\.)*\"", 0, styleString);
        applyPattern(doc, text, "'([^\\'\\r\\n]|\\\\.)*'", 0, styleString);
        // 4) 注解 @Identifier
        applyPattern(doc, text, "@[_$A-Za-z][_$A-Za-z0-9]*", 0, styleAnnotation);
        // 5) 关键字（词边界）
        String keywordRegex = buildKeywordRegex();
        applyPattern(doc, text, keywordRegex, 0, styleKeyword);
    }

    private void applyPattern(StyledDocument doc, String text, String regex, int flags, Style style) {
        Pattern p = Pattern.compile(regex, flags);
        Matcher m = p.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            doc.setCharacterAttributes(start, end - start, style, true);
        }
    }

    private String buildKeywordRegex() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\b(");
        for (int i = 0; i < JAVA_KEYWORDS.length; i++) {
            if (i > 0) sb.append("|");
            sb.append(Pattern.quote(JAVA_KEYWORDS[i]));
        }
        sb.append(")\\b");
        return sb.toString();
    }
} 