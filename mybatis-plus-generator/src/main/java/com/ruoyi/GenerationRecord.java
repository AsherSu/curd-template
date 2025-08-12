package com.ruoyi;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 代码生成记录实体类
 */
public class GenerationRecord {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 生成时间
     */
    private LocalDateTime generationTime;
    
    /**
     * 表名列表
     */
    private String[] tableNames;
    
    /**
     * 模块名
     */
    private String moduleName;
    
    /**
     * 父包名
     */
    private String parentPackage;
    
    /**
     * 是否生成Controller
     */
    private boolean enableController;
    
    /**
     * 数据库URL
     */
    private String dbUrl;
    
    /**
     * 生成结果状态
     */
    private String status; // SUCCESS, FAILED, CANCELLED
    
    /**
     * 生成的文件列表
     */
    private List<GeneratedFile> generatedFiles;

    // 新增：自定义输出目录（可为空）
    private String entityDir;
    private String mapperDir;
    private String serviceDir;
    private String serviceImplDir;
    private String controllerDir;
    private String xmlDir;

    public GenerationRecord() {
        this.generationTime = LocalDateTime.now();
        this.status = "UNKNOWN";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getGenerationTime() { return generationTime; }
    public void setGenerationTime(LocalDateTime generationTime) { this.generationTime = generationTime; }

    public String[] getTableNames() { return tableNames; }
    public void setTableNames(String[] tableNames) { this.tableNames = tableNames; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getParentPackage() { return parentPackage; }
    public void setParentPackage(String parentPackage) { this.parentPackage = parentPackage; }

    public boolean isEnableController() { return enableController; }
    public void setEnableController(boolean enableController) { this.enableController = enableController; }

    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<GeneratedFile> getGeneratedFiles() { return generatedFiles; }
    public void setGeneratedFiles(List<GeneratedFile> generatedFiles) { this.generatedFiles = generatedFiles; }

    // 新增目录字段 getter/setter
    public String getEntityDir() { return entityDir; }
    public void setEntityDir(String entityDir) { this.entityDir = entityDir; }

    public String getMapperDir() { return mapperDir; }
    public void setMapperDir(String mapperDir) { this.mapperDir = mapperDir; }

    public String getServiceDir() { return serviceDir; }
    public void setServiceDir(String serviceDir) { this.serviceDir = serviceDir; }

    public String getServiceImplDir() { return serviceImplDir; }
    public void setServiceImplDir(String serviceImplDir) { this.serviceImplDir = serviceImplDir; }

    public String getControllerDir() { return controllerDir; }
    public void setControllerDir(String controllerDir) { this.controllerDir = controllerDir; }

    public String getXmlDir() { return xmlDir; }
    public void setXmlDir(String xmlDir) { this.xmlDir = xmlDir; }

    @Override
    public String toString() {
        return "GenerationRecord{" +
                "id=" + id +
                ", generationTime=" + generationTime +
                ", tableNames=" + Arrays.toString(tableNames) +
                ", moduleName='" + moduleName + '\'' +
                ", parentPackage='" + parentPackage + '\'' +
                ", enableController=" + enableController +
                ", dbUrl='" + dbUrl + '\'' +
                ", status='" + status + '\'' +
                ", generatedFilesCount=" + (generatedFiles != null ? generatedFiles.size() : 0) +
                '}';
    }
}