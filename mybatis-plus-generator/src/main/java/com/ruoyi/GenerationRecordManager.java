package com.ruoyi;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 生成记录管理类
 */
public class GenerationRecordManager {
    private static final String RECORD_FILE = "generation_records.json";
    private static final AtomicLong idGenerator = new AtomicLong(1);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 保存生成记录
     * 
     * @param record 生成记录
     */
    public static void saveRecord(GenerationRecord record) {
        try {
            List<GenerationRecord> records = loadRecords();
            
            // 设置ID
            if (record.getId() == null) {
                record.setId(idGenerator.getAndIncrement());
            }
            
            records.add(record);
            
            // 只保留最近100条记录
            if (records.size() > 100) {
                records = records.subList(records.size() - 100, records.size());
            }
            
            writeRecords(records);
        } catch (Exception e) {
            System.err.println("保存生成记录失败: " + e.getMessage());
        }
    }

    /**
     * 加载所有生成记录
     * 
     * @return 生成记录列表
     */
    public static List<GenerationRecord> loadRecords() {
        try {
            Path path = Paths.get(RECORD_FILE);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            
            String content = new String(Files.readAllBytes(path));
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            GenerationRecord[] records = objectMapper.readValue(content, GenerationRecord[].class);
            
            // 更新ID生成器以避免ID冲突
            for (GenerationRecord record : records) {
                if (record.getId() != null && record.getId() >= idGenerator.get()) {
                    idGenerator.set(record.getId() + 1);
                }
            }
            
            List<GenerationRecord> result = new ArrayList<>();
            for (GenerationRecord record : records) {
                result.add(record);
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("加载生成记录失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 写入生成记录到文件
     * 
     * @param records 生成记录列表
     */
    private static void writeRecords(List<GenerationRecord> records) {
        try {
            String json = objectMapper.writeValueAsString(records.toArray(new GenerationRecord[0]));
            Files.write(Paths.get(RECORD_FILE), json.getBytes());
        } catch (Exception e) {
            System.err.println("写入生成记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查找记录
     * 
     * @param id 记录ID
     * @return 生成记录
     */
    public static GenerationRecord findById(Long id) {
        List<GenerationRecord> records = loadRecords();
        return records.stream()
                .filter(record -> record.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 删除记录
     * 
     * @param id 记录ID
     */
    public static void deleteRecord(Long id) {
        try {
            List<GenerationRecord> records = loadRecords();
            records.removeIf(record -> record.getId().equals(id));
            writeRecords(records);
        } catch (Exception e) {
            System.err.println("删除记录失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有记录
     */
    public static void clearAllRecords() {
        try {
            Files.write(Paths.get(RECORD_FILE), new byte[0]);
            idGenerator.set(1);
        } catch (Exception e) {
            System.err.println("清空记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 格式化显示记录
     * 
     * @param record 生成记录
     * @return 格式化字符串
     */
    public static String formatRecord(GenerationRecord record) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(
            "ID: %d | 时间: %s | 模块: %s | 包名: %s | 表数: %d | Controller: %s | 状态: %s",
            record.getId(),
            record.getGenerationTime().format(formatter),
            record.getModuleName(),
            record.getParentPackage(),
            record.getTableNames() != null ? record.getTableNames().length : 0,
            record.isEnableController() ? "是" : "否",
            record.getStatus()
        );
    }
    
    /**
     * 检查文件是否存在且内容相同
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     * @return 检查结果：0-文件不存在，1-文件存在且内容相同，2-文件存在但内容不同
     */
    public static int checkFileExistsAndCompare(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return 0; // 文件不存在
            }
            
            String existingContent = new String(Files.readAllBytes(path));
            if (existingContent.equals(content)) {
                return 1; // 文件存在且内容相同
            } else {
                return 2; // 文件存在但内容不同
            }
        } catch (IOException e) {
            System.err.println("检查文件时出错: " + e.getMessage());
            return 0; // 出错时视为文件不存在
        }
    }

    /**
     * 更新一条记录（按ID替换）
     */
    public static void updateRecord(GenerationRecord updated) {
        if (updated == null || updated.getId() == null) return;
        try {
            List<GenerationRecord> records = loadRecords();
            boolean replaced = false;
            for (int i = 0; i < records.size(); i++) {
                if (updated.getId().equals(records.get(i).getId())) {
                    records.set(i, updated);
                    replaced = true;
                    break;
                }
            }
            // 若不存在则追加并设置ID增长
            if (!replaced) {
                if (updated.getId() >= idGenerator.get()) {
                    idGenerator.set(updated.getId() + 1);
                }
                records.add(updated);
            }
            writeRecords(records);
        } catch (Exception e) {
            System.err.println("更新生成记录失败: " + e.getMessage());
        }
    }
}