package com.ruoyi.core;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.ruoyi.CodeGenerator;
import com.ruoyi.DbConnectionInfo;
import com.ruoyi.GeneratedFile;
import com.ruoyi.GenerationPathConfig;
import com.ruoyi.GenerationRecord;
import com.ruoyi.contracts.GenerateRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 代码生成核心服务：
 * - 构建并执行 MyBatis-Plus 生成
 * - 统一路径解析与目录创建
 * - 收集生成的文件列表
 */
public class GeneratorService {

    public void generate(GenerateRequest request, GenerationRecord record) {
        Objects.requireNonNull(request, "request");
        String[] tableNames = Objects.requireNonNull(request.getTableNames(), "tables");
        String parentPackage = Objects.requireNonNull(request.getParentPackage(), "parentPackage");
        String moduleName = Objects.requireNonNull(request.getModuleName(), "moduleName");
        boolean enableController = request.isEnableController();
        GenerationPathConfig customPaths = request.getPathConfig();

        // 数据源：若请求未显式提供连接，则沿用 CodeGenerator 中的配置/活动连接
        DbConnectionInfo conn = request.getConnection();
        DataSourceConfig.Builder dataSourceConfigBuilder = (conn == null)
                ? new DataSourceConfig.Builder(CodeGenerator.getDbUrl(), CodeGenerator.getDbUsername(), CodeGenerator.getDbPassword())
                : new DataSourceConfig.Builder(conn.getUrl(), conn.getUsername(), conn.getPassword());

        // 解析输出路径
        PathResolver resolver = new PathResolver(CodeGenerator.getOutputDir(), parentPackage, moduleName, customPaths, enableController);
        resolver.ensureAll();
        Map<OutputFile, String> pathInfo = resolver.toPathInfoMap();

        // 执行生成
        FastAutoGenerator.create(dataSourceConfigBuilder)
                .globalConfig(builder -> builder
                        .outputDir(CodeGenerator.getOutputDir())
                        .author("generator")
                        .disableOpenDir()
                        .build())
                .packageConfig(builder -> builder
                        .parent(parentPackage)
                        .moduleName(moduleName)
                        .entity("entity")
                        .service("service")
                        .serviceImpl("service.impl")
                        .mapper("mapper")
                        .xml("mapper.xml")
                        .controller(enableController ? "controller" : null)
                        .pathInfo(pathInfo)
                        .build())
                .strategyConfig(builder -> builder
                        .addInclude(tableNames)
                        .entityBuilder()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .enableLombok()
                        .logicDeleteColumnName("deleted")
                        .build()
                        .controllerBuilder()
                        .enableRestStyle()
                        .enableHyphenStyle()
                        .build()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                        .build()
                        .mapperBuilder()
                        .enableMapperAnnotation()
                        .enableBaseResultMap()
                        .enableBaseColumnList()
                        .build())
                .templateEngine(new VelocityTemplateEngine())
                .execute();

        // 收集文件
        List<GeneratedFile> generatedFiles = new ArrayList<>();
        collectFromDirIfExists(generatedFiles, resolver.getEntityDir(), "ENTITY");
        collectFromDirIfExists(generatedFiles, resolver.getMapperDir(), "MAPPER");
        collectFromDirIfExists(generatedFiles, resolver.getServiceDir(), "SERVICE");
        collectFromDirIfExists(generatedFiles, resolver.getServiceImplDir(), "SERVICE_IMPL");
        if (enableController) collectFromDirIfExists(generatedFiles, resolver.getControllerDir(), "CONTROLLER");
        collectFromDirIfExists(generatedFiles, resolver.getXmlDir(), "XML");
        record.setGeneratedFiles(generatedFiles);
    }

    private void collectFromDirIfExists(List<GeneratedFile> list, String dir, String type) {
        if (dir == null) return;
        try {
            Path p = Paths.get(dir);
            if (!Files.exists(p)) return;
            Files.walk(p).filter(Files::isRegularFile).forEach(path -> {
                try {
                    String content = new String(Files.readAllBytes(path));
                    list.add(new GeneratedFile(path.toString(), content, type));
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }
} 