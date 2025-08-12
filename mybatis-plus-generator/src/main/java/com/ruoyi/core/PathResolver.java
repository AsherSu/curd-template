package com.ruoyi.core;

import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.ruoyi.GenerationPathConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PathResolver {
    private final String outputDir;
    private final String parentPackage;
    private final String moduleName;
    private final GenerationPathConfig custom;
    private final boolean enableController;

    private final String basePackageDir;

    private String entityDir;
    private String mapperDir;
    private String serviceDir;
    private String serviceImplDir;
    private String controllerDir;
    private String xmlDir;

    public PathResolver(String outputDir, String parentPackage, String moduleName, GenerationPathConfig custom, boolean enableController) {
        this.outputDir = outputDir;
        this.parentPackage = parentPackage;
        this.moduleName = moduleName;
        this.custom = custom;
        this.enableController = enableController;
        this.basePackageDir = outputDir + "/" + parentPackage.replace(".", "/") + "/" + moduleName;
        resolveAll();
    }

    private void resolveAll() {
        this.entityDir = chooseDir(custom == null ? null : custom.getEntityDir(), basePackageDir + "/entity");
        this.mapperDir = chooseDir(custom == null ? null : custom.getMapperDir(), basePackageDir + "/mapper");
        this.serviceDir = chooseDir(custom == null ? null : custom.getServiceDir(), basePackageDir + "/service");
        this.serviceImplDir = chooseDir(custom == null ? null : custom.getServiceImplDir(), basePackageDir + "/service/impl");
        if (enableController) {
            this.controllerDir = chooseDir(custom == null ? null : custom.getControllerDir(), basePackageDir + "/controller");
        }
        String defaultXml = System.getProperty("user.dir") + "/src/main/resources/mapper/" + moduleName;
        this.xmlDir = chooseDir(custom == null ? null : custom.getXmlDir(), defaultXml);
    }

    private String chooseDir(String preferred, String fallback) {
        return (preferred != null && !preferred.trim().isEmpty()) ? preferred : fallback;
    }

    public void ensureAll() {
        ensure(entityDir);
        ensure(mapperDir);
        ensure(serviceDir);
        ensure(serviceImplDir);
        ensure(xmlDir);
        if (enableController && controllerDir != null) ensure(controllerDir);
    }

    private void ensure(String dir) {
        if (dir == null) return;
        try {
            Path p = Paths.get(dir);
            if (!Files.exists(p)) Files.createDirectories(p);
        } catch (IOException ignored) {}
    }

    public Map<OutputFile, String> toPathInfoMap() {
        Map<OutputFile, String> map = new HashMap<>();
        map.put(OutputFile.xml, xmlDir);
        map.put(OutputFile.entity, entityDir);
        map.put(OutputFile.mapper, mapperDir);
        map.put(OutputFile.service, serviceDir);
        map.put(OutputFile.serviceImpl, serviceImplDir);
        if (enableController && controllerDir != null) map.put(OutputFile.controller, controllerDir);
        return map;
    }

    public String getEntityDir() { return entityDir; }
    public String getMapperDir() { return mapperDir; }
    public String getServiceDir() { return serviceDir; }
    public String getServiceImplDir() { return serviceImplDir; }
    public String getControllerDir() { return controllerDir; }
    public String getXmlDir() { return xmlDir; }
} 