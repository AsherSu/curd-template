package com.ruoyi;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 代码生成器主类
 * 可直接运行main方法生成代码
 *
 * @author generator
 * @since 3.5.3
 */
public class CodeGenerator {

    /**
     * 数据库连接配置
     */
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;

    /**
     * 代码生成根路径
     */
    private static String OUTPUT_DIR;

    /**
     * 默认父包名
     */
    private static String DEFAULT_PARENT_PACKAGE;

    public static String getDbDriver() {
        DbConnectionInfo active = ConnectionRepository.getActive().orElse(null);
        return active != null ? active.getDriverClassName() : DB_DRIVER;
    }

    public static String getDbUrl() {
        DbConnectionInfo active = ConnectionRepository.getActive().orElse(null);
        return active != null ? active.getUrl() : DB_URL;
    }

    public static String getDbUsername() {
        DbConnectionInfo active = ConnectionRepository.getActive().orElse(null);
        return active != null ? active.getUsername() : DB_USERNAME;
    }

    public static String getDbPassword() {
        DbConnectionInfo active = ConnectionRepository.getActive().orElse(null);
        return active != null ? active.getPassword() : DB_PASSWORD;
    }

    public static String getOutputDir() {
        return OUTPUT_DIR;
    }

    public static String getDefaultParentPackage() {
        return DEFAULT_PARENT_PACKAGE;
    }

    static {
        loadProperties();
    }

    /**
     * 加载配置文件
     */
    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = CodeGenerator.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("无法找到 application.properties 配置文件!");
                return;
            }
            props.load(input);

            // 数据库配置
            DB_URL = props.getProperty("db.url");
            DB_USERNAME = props.getProperty("db.username");
            DB_PASSWORD = props.getProperty("db.password");
            DB_DRIVER = props.getProperty("db.driver-class-name");

            // 代码生成配置
            OUTPUT_DIR = props.getProperty("output.dir");
            DEFAULT_PARENT_PACKAGE = props.getProperty("default.parent.package");

            // 处理系统属性变量
            if (OUTPUT_DIR != null && OUTPUT_DIR.contains("${user.dir}")) {
                OUTPUT_DIR = OUTPUT_DIR.replace("${user.dir}", System.getProperty("user.dir"));
            }
        } catch (IOException ex) {
            System.out.println("加载配置文件时出错: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            showMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    generateCodeInteractive(scanner);
                    break;
                case "2":
                    showGenerationHistory(scanner);
                    break;
                case "3":
                    exit = true;
                    System.out.println("感谢使用，再见！");
                    break;
                default:
                    System.out.println("无效选项，请重新选择。");
                    break;
            }
        }
    }

    /**
     * 显示主菜单
     */
    private static void showMainMenu() {
        System.out.println("\n===== MyBatis-Plus 代码生成器 =====");
        System.out.println("1. 生成代码");
        System.out.println("2. 查看生成记录");
        System.out.println("3. 退出");
        System.out.print("请选择操作: ");
    }

    /**
     * 交互式生成代码
     */
    private static void generateCodeInteractive(Scanner scanner) {
        System.out.println("\n===== 代码生成 =====");
        System.out.println("数据库URL: " + getDbUrl());
        System.out.println("代码输出目录: " + OUTPUT_DIR);
        System.out.println("默认包名: " + DEFAULT_PARENT_PACKAGE);
        System.out.println();

        // 获取数据库中的所有表
        List<String> tableList = getTableList();
        if (tableList.isEmpty()) {
            System.out.println("未找到任何数据表，返回主菜单。");
            return;
        }

        // 显示所有表
        System.out.println("数据库中的表:");
        for (int i = 0; i < tableList.size(); i++) {
            System.out.println((i + 1) + ". " + tableList.get(i));
        }
        System.out.println();

        // 获取用户选择的表
        String[] tableNames = getSelectedTables(scanner, tableList);
        if (tableNames.length == 0) {
            System.out.println("未选择任何表，返回主菜单。");
            return;
        }

        // 获取模块名
        System.out.print("请输入模块名称（用于包名，如 system）: ");
        String moduleName = scanner.nextLine();
        if (moduleName == null || moduleName.trim().isEmpty()) {
            moduleName = "module";
        }

        // 获取父包名
        System.out.print("请输入父包名（默认: " + DEFAULT_PARENT_PACKAGE + "）: ");
        String parentPackage = scanner.nextLine();
        if (parentPackage == null || parentPackage.trim().isEmpty()) {
            parentPackage = DEFAULT_PARENT_PACKAGE;
        }

        // 获取是否生成Controller
        System.out.print("是否生成Controller？(y/n, 默认为y): ");
        String generateController = scanner.nextLine();
        boolean enableController = generateController.isEmpty() || 
                generateController.toLowerCase().startsWith("y");

        // 创建生成记录
        GenerationRecord record = new GenerationRecord();
        record.setTableNames(tableNames);
        record.setModuleName(moduleName);
        record.setParentPackage(parentPackage);
        record.setEnableController(enableController);
        record.setDbUrl(getDbUrl());
        record.setStatus("PROCESSING");

        System.out.println("\n开始生成代码...");

        try {
            // 记录生成前的时间戳，用于后续删除操作
            long startTime = System.currentTimeMillis();
            
            generateCode(tableNames, parentPackage, moduleName, enableController, record);
            System.out.println("代码生成完成！");
            
            record.setStatus("SUCCESS");
            
            // 保存生成记录
            GenerationRecordManager.saveRecord(record);
            
            // 询问是否确认生成的代码
                            System.out.print("是否确认保留生成的代码？(y/n, 默认为y): ");
                String confirm = scanner.nextLine();
                if (!confirm.isEmpty() && confirm.toLowerCase().startsWith("n")) {
                    System.out.println("正在删除刚刚生成的文件...");
                    new com.ruoyi.core.RecordService().deleteFilesOfRecord(record);
                    System.out.println("已删除生成的文件。");
                    record.setStatus("CANCELLED");
                    // 更新记录状态
                    GenerationRecordManager.saveRecord(record);
                } else {
                    System.out.println("代码已保留。");
                }
        } catch (Exception e) {
            System.err.println("代码生成失败: " + e.getMessage());
            e.printStackTrace();
            record.setStatus("FAILED");
            // 保存失败记录
            GenerationRecordManager.saveRecord(record);
        }
    }

    /**
     * 显示生成历史记录
     */
    private static void showGenerationHistory(Scanner scanner) {
        System.out.println("\n===== 生成记录 =====");
        List<GenerationRecord> records = GenerationRecordManager.loadRecords();
        
        if (records.isEmpty()) {
            System.out.println("暂无生成记录。");
            return;
        }
        
        // 显示最近的记录（最多显示20条）
        int displayCount = Math.min(records.size(), 20);
        System.out.println("最近 " + displayCount + " 条记录:");
        for (int i = records.size() - displayCount; i < records.size(); i++) {
            GenerationRecord record = records.get(i);
            System.out.println((i + 1) + ". " + GenerationRecordManager.formatRecord(record));
        }
        
        System.out.println("\n操作选项:");
        System.out.println("1. 查看详细信息");
        System.out.println("2. 恢复记录文件");
        System.out.println("3. 删除记录");
        System.out.println("4. 清空所有记录");
        System.out.println("5. 返回主菜单");
        System.out.print("请选择操作: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                showRecordDetails(scanner, records);
                break;
            case "2":
                restoreRecordFiles(scanner, records);
                break;
            case "3":
                deleteRecord(scanner, records);
                break;
            case "4":
                clearAllRecords(scanner);
                break;
            case "5":
                // 返回主菜单
                break;
            default:
                System.out.println("无效选项。");
                break;
        }
    }
    
    /**
     * 显示记录详细信息
     */
    private static void showRecordDetails(Scanner scanner, List<GenerationRecord> records) {
        System.out.print("请输入记录ID: ");
        try {
            long id = Long.parseLong(scanner.nextLine().trim());
            GenerationRecord record = GenerationRecordManager.findById(id);
            
            if (record == null) {
                System.out.println("未找到ID为 " + id + " 的记录。");
                return;
            }
            
            System.out.println("\n===== 记录详情 =====");
            System.out.println("ID: " + record.getId());
            System.out.println("生成时间: " + record.getGenerationTime());
            System.out.println("数据库URL: " + record.getDbUrl());
            System.out.println("模块名: " + record.getModuleName());
            System.out.println("父包名: " + record.getParentPackage());
            System.out.println("生成Controller: " + (record.isEnableController() ? "是" : "否"));
            System.out.println("状态: " + record.getStatus());
            System.out.println("表名列表: ");
            if (record.getTableNames() != null) {
                for (String tableName : record.getTableNames()) {
                    System.out.println("  - " + tableName);
                }
            }
            
            System.out.println("生成文件列表: ");
            if (record.getGeneratedFiles() != null && !record.getGeneratedFiles().isEmpty()) {
                for (GeneratedFile file : record.getGeneratedFiles()) {
                    System.out.println("  - [" + file.getFileType() + "] " + file.getFilePath());
                }
            } else {
                System.out.println("  暂无文件信息");
            }
        } catch (NumberFormatException e) {
            System.out.println("无效的ID格式。");
        }
    }
    
    /**
     * 恢复记录文件
     */
    private static void restoreRecordFiles(Scanner scanner, List<GenerationRecord> records) {
        System.out.print("请输入要恢复的记录ID: ");
        try {
            long id = Long.parseLong(scanner.nextLine().trim());
            GenerationRecord record = GenerationRecordManager.findById(id);
            
            if (record == null) {
                System.out.println("未找到ID为 " + id + " 的记录。");
                return;
            }
            
            if (record.getGeneratedFiles() == null || record.getGeneratedFiles().isEmpty()) {
                System.out.println("该记录没有保存文件信息，无法恢复。");
                return;
            }
            
            System.out.println("即将恢复 " + record.getGeneratedFiles().size() + " 个文件:");
            for (GeneratedFile file : record.getGeneratedFiles()) {
                System.out.println("  - [" + file.getFileType() + "] " + file.getFilePath());
            }
            
            System.out.print("确认恢复这些文件? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.toLowerCase().startsWith("y")) {
                System.out.println("取消恢复操作。");
                return;
            }
            
            // 恢复文件
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
                        System.out.println("已创建文件: " + filePath);
                        break;
                    case 1: // 文件存在且内容相同
                        System.out.println("跳过文件 (内容相同): " + filePath);
                        skippedCount++;
                        break;
                    case 2: // 文件存在但内容不同
                        System.out.print("文件内容不同: " + filePath + "，是否覆盖? (y/n): ");
                        String overwrite = scanner.nextLine().trim();
                        if (overwrite.toLowerCase().startsWith("y")) {
                            createFileWithContent(filePath, content);
                            overwrittenCount++;
                            System.out.println("已覆盖文件: " + filePath);
                        } else {
                            System.out.println("跳过文件: " + filePath);
                            skippedCount++;
                        }
                        break;
                }
            }
            
            System.out.println("\n恢复完成:");
            System.out.println("  新建文件: " + restoredCount);
            System.out.println("  覆盖文件: " + overwrittenCount);
            System.out.println("  跳过文件: " + skippedCount);
            
        } catch (NumberFormatException e) {
            System.out.println("无效的ID格式。");
        }
    }
    
    /**
     * 创建文件并写入内容
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     */
    private static void createFileWithContent(String filePath, String content) {
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
            System.err.println("创建文件失败: " + filePath + " - " + e.getMessage());
        }
    }
    
    /**
     * 删除记录
     */
    private static void deleteRecord(Scanner scanner, List<GenerationRecord> records) {
        System.out.print("请输入要删除的记录ID: ");
        try {
            long id = Long.parseLong(scanner.nextLine().trim());
            GenerationRecord record = GenerationRecordManager.findById(id);
            
            if (record == null) {
                System.out.println("未找到ID为 " + id + " 的记录。");
                return;
            }
            
            System.out.print("确认删除记录 ID " + id + " ? (y/n): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.toLowerCase().startsWith("y")) {
                GenerationRecordManager.deleteRecord(id);
                System.out.println("记录已删除。");
            } else {
                System.out.println("取消删除。");
            }
        } catch (NumberFormatException e) {
            System.out.println("无效的ID格式。");
        }
    }
    
    /**
     * 清空所有记录
     */
    private static void clearAllRecords(Scanner scanner) {
        System.out.print("确认清空所有记录? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.toLowerCase().startsWith("y")) {
            GenerationRecordManager.clearAllRecords();
            System.out.println("所有记录已清空。");
        } else {
            System.out.println("取消清空。");
        }
    }

    /**
     * 获取数据库中的所有表名
     *
     * @return 表名列表
     */
    public static List<String> getTableList() {
        List<String> tableList = new ArrayList<>();
        Connection conn = null;
        try {
            // 加载MySQL驱动
            Class.forName(getDbDriver());
            conn = DriverManager.getConnection(getDbUrl(), getDbUsername(), getDbPassword());
            DatabaseMetaData metaData = conn.getMetaData();

            // 从URL中提取数据库名
            String dbName = extractDatabaseName(getDbUrl());
            
            System.out.println("正在连接数据库并获取表列表...");
            
            ResultSet rs = metaData.getTables(dbName, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableList.add(tableName);
            }
            rs.close();
            
            System.out.println("成功获取到 " + tableList.size() + " 个表");
        } catch (Exception e) {
            System.err.println("获取表列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("关闭数据库连接失败: " + e.getMessage());
                }
            }
        }
        return tableList;
    }
    
    /**
     * 从数据库URL中提取数据库名
     * 
     * @param url 数据库URL
     * @return 数据库名
     */
    public static String extractDatabaseName(String url) {
        String dbName = url.substring(url.lastIndexOf("/") + 1);
        if (dbName.contains("?")) {
            dbName = dbName.substring(0, dbName.indexOf("?"));
        }
        return dbName;
    }

    /**
     * 获取用户选择的表
     *
     * @param scanner    Scanner对象
     * @param tableList  表列表
     * @return 选择的表名数组
     */
    private static String[] getSelectedTables(Scanner scanner, List<String> tableList) {
        System.out.println("请选择要生成代码的表:");
        System.out.println("输入序号选择单个表，多个表用英文逗号分隔，如: 1,3,5");
        System.out.println("输入'all'选择所有表");
        System.out.print("请输入: ");
        
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return new String[0];
        }

        // 选择所有表
        if ("all".equalsIgnoreCase(input)) {
            return tableList.toArray(new String[0]);
        }

        // 解析用户输入
        String[] selections = input.split(",");
        List<String> selectedTables = new ArrayList<>();
        
        for (String selection : selections) {
            selection = selection.trim();
            try {
                int index = Integer.parseInt(selection);
                if (index > 0 && index <= tableList.size()) {
                    selectedTables.add(tableList.get(index - 1));
                } else {
                    System.out.println("警告: 序号 " + index + " 超出范围，已忽略");
                }
            } catch (NumberFormatException e) {
                System.out.println("警告: '" + selection + "' 不是有效的数字，已忽略");
            }
        }
        
        return selectedTables.toArray(new String[0]);
    }

    /**
     * 执行代码生成（保留原有签名）
     */
    public static void generateCode(String[] tableNames, String parentPackage, 
                                   String moduleName, boolean enableController, GenerationRecord record) {
        com.ruoyi.core.GeneratorService service = new com.ruoyi.core.GeneratorService();
        com.ruoyi.contracts.GenerateRequest req = new com.ruoyi.contracts.GenerateRequest(
                tableNames,
                parentPackage,
                moduleName,
                enableController,
                null,
                ConnectionRepository.getActive().orElse(null)
        );
        service.generate(req, record);
    }

    /**
     * 执行代码生成（支持自定义输出路径）
     */
    public static void generateCode(String[] tableNames, String parentPackage,
                                    String moduleName, boolean enableController,
                                    GenerationRecord record, GenerationPathConfig customPaths) {
        com.ruoyi.core.GeneratorService service = new com.ruoyi.core.GeneratorService();
        com.ruoyi.contracts.GenerateRequest req = new com.ruoyi.contracts.GenerateRequest(
                tableNames,
                parentPackage,
                moduleName,
                enableController,
                customPaths,
                ConnectionRepository.getActive().orElse(null)
        );
        service.generate(req, record);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static void ensureDir(String dir) {
        try {
            if (dir == null) return;
            Path p = Paths.get(dir);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
        } catch (IOException e) {
            System.err.println("创建目录失败: " + dir + " - " + e.getMessage());
        }
    }

    /**
     * 收集生成的文件（默认路径版，保留兼容）
     */
    private static void collectGeneratedFiles(List<GeneratedFile> generatedFiles, String parentPackage, 
                                            String moduleName, String[] tableNames, boolean enableController) {
        try {
            String basePath = OUTPUT_DIR + "/" + parentPackage.replace(".", "/") + "/" + moduleName;
            collectFilesFromDirectory(generatedFiles, basePath + "/entity", "ENTITY");
            collectFilesFromDirectory(generatedFiles, basePath + "/mapper", "MAPPER");
            collectFilesFromDirectory(generatedFiles, basePath + "/service", "SERVICE");
            collectFilesFromDirectory(generatedFiles, basePath + "/service/impl", "SERVICE_IMPL");
            if (enableController) {
                collectFilesFromDirectory(generatedFiles, basePath + "/controller", "CONTROLLER");
            }
            String xmlPath = System.getProperty("user.dir") + "/src/main/resources/mapper/" + moduleName;
            collectFilesFromDirectory(generatedFiles, xmlPath, "XML");
        } catch (Exception e) {
            System.err.println("收集生成文件失败: " + e.getMessage());
        }
    }

    /**
     * 收集生成的文件（自定义路径版）
     */
    private static void collectGeneratedFiles(List<GeneratedFile> generatedFiles, GenerationPathConfig usedPaths, boolean enableController) {
        try {
            if (usedPaths.getEntityDir() != null) collectFilesFromDirectory(generatedFiles, usedPaths.getEntityDir(), "ENTITY");
            if (usedPaths.getMapperDir() != null) collectFilesFromDirectory(generatedFiles, usedPaths.getMapperDir(), "MAPPER");
            if (usedPaths.getServiceDir() != null) collectFilesFromDirectory(generatedFiles, usedPaths.getServiceDir(), "SERVICE");
            if (usedPaths.getServiceImplDir() != null) collectFilesFromDirectory(generatedFiles, usedPaths.getServiceImplDir(), "SERVICE_IMPL");
            if (enableController && usedPaths.getControllerDir() != null) collectFilesFromDirectory(generatedFiles, usedPaths.getControllerDir(), "CONTROLLER");
            if (usedPaths.getXmlDir() != null) collectFilesFromDirectory(generatedFiles, usedPaths.getXmlDir(), "XML");
        } catch (Exception e) {
            System.err.println("收集生成文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 从目录中收集文件
     * 
     * @param generatedFiles 文件列表
     * @param directoryPath 目录路径
     * @param fileType 文件类型
     */
    private static void collectFilesFromDirectory(List<GeneratedFile> generatedFiles, String directoryPath, String fileType) {
        try {
            Path dir = Paths.get(directoryPath);
            if (Files.exists(dir) && Files.isDirectory(dir)) {
                Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String content = new String(Files.readAllBytes(path));
                            String relativePath = path.toString();
                            generatedFiles.add(new GeneratedFile(relativePath, content, fileType));
                        } catch (IOException e) {
                            System.err.println("读取文件失败: " + path + " - " + e.getMessage());
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("遍历目录失败: " + directoryPath + " - " + e.getMessage());
        }
    }
    
    /**
     * 删除生成的文件
     * 
     * @param parentPackage 父包名
     * @param moduleName 模块名
     * @param tableNames 表名数组
     * @param hasController 是否生成了Controller
     * @param outputDir 输出目录
     * @param startTime 开始时间戳
     */
    private static void deleteGeneratedFiles(String parentPackage, String moduleName, 
                                           String[] tableNames, boolean hasController,
                                           String outputDir, long startTime) {
        try {
            String basePath = outputDir + "/" + parentPackage.replace(".", "/") + "/" + moduleName;
            
            // 删除实体类文件
            deleteFilesByPattern(basePath + "/entity", startTime);
            
            // 删除Mapper接口文件
            deleteFilesByPattern(basePath + "/mapper", startTime);
            
            // 删除Mapper XML文件
            String xmlPath = System.getProperty("user.dir") + "/src/main/resources/mapper/" + moduleName;
            deleteFilesByPattern(xmlPath, startTime);
            
            // 删除Service文件
            deleteFilesByPattern(basePath + "/service", startTime);
            
            // 删除Controller文件（如果生成了）
            if (hasController) {
                deleteFilesByPattern(basePath + "/controller", startTime);
            }
            
            System.out.println("文件删除完成。");
        } catch (Exception e) {
            System.err.println("删除文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根据时间戳删除指定目录中在指定时间后创建的文件
     * 
     * @param dirPath 目录路径
     * @param startTime 时间戳
     */
    private static void deleteFilesByPattern(String dirPath, long startTime) {
        try {
            Path dir = Paths.get(dirPath);
            if (Files.exists(dir) && Files.isDirectory(dir)) {
                Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() >= startTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("已删除文件: " + path);
                        } catch (IOException e) {
                            System.err.println("删除文件失败: " + path + " - " + e.getMessage());
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("遍历目录失败: " + dirPath + " - " + e.getMessage());
        }
    }
}