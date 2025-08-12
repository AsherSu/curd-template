package com.ruoyi.contracts;

import com.ruoyi.DbConnectionInfo;
import com.ruoyi.GenerationPathConfig;

/**
 * 代码生成请求参数
 */
public class GenerateRequest {
    private String[] tableNames;
    private String parentPackage;
    private String moduleName;
    private boolean enableController;
    private GenerationPathConfig pathConfig; // 可选：自定义输出路径
    private DbConnectionInfo connection;     // 可选：指定连接；为空则走活动连接/配置

    public GenerateRequest() {}

    public GenerateRequest(String[] tableNames,
                           String parentPackage,
                           String moduleName,
                           boolean enableController,
                           GenerationPathConfig pathConfig,
                           DbConnectionInfo connection) {
        this.tableNames = tableNames;
        this.parentPackage = parentPackage;
        this.moduleName = moduleName;
        this.enableController = enableController;
        this.pathConfig = pathConfig;
        this.connection = connection;
    }

    public String[] getTableNames() { return tableNames; }
    public void setTableNames(String[] tableNames) { this.tableNames = tableNames; }

    public String getParentPackage() { return parentPackage; }
    public void setParentPackage(String parentPackage) { this.parentPackage = parentPackage; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public boolean isEnableController() { return enableController; }
    public void setEnableController(boolean enableController) { this.enableController = enableController; }

    public GenerationPathConfig getPathConfig() { return pathConfig; }
    public void setPathConfig(GenerationPathConfig pathConfig) { this.pathConfig = pathConfig; }

    public DbConnectionInfo getConnection() { return connection; }
    public void setConnection(DbConnectionInfo connection) { this.connection = connection; }
} 