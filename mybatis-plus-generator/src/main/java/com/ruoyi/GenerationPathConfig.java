package com.ruoyi;

/**
 * 代码生成输出路径配置
 */
public class GenerationPathConfig {
    private String entityDir;
    private String mapperDir;
    private String serviceDir;
    private String serviceImplDir;
    private String controllerDir;
    private String xmlDir;

    public GenerationPathConfig() {}

    public GenerationPathConfig(String entityDir, String mapperDir, String serviceDir,
                                String serviceImplDir, String controllerDir, String xmlDir) {
        this.entityDir = entityDir;
        this.mapperDir = mapperDir;
        this.serviceDir = serviceDir;
        this.serviceImplDir = serviceImplDir;
        this.controllerDir = controllerDir;
        this.xmlDir = xmlDir;
    }

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
} 