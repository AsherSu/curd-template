package com.ruoyi;

/**
 * 生成文件实体类
 */
public class GeneratedFile {
    /**
     * 文件相对路径
     */
    private String filePath;
    
    /**
     * 文件内容
     */
    private String content;
    
    /**
     * 文件类型
     */
    private String fileType; // ENTITY, MAPPER, SERVICE, SERVICE_IMPL, CONTROLLER, XML
    
    public GeneratedFile() {
    }
    
    public GeneratedFile(String filePath, String content, String fileType) {
        this.filePath = filePath;
        this.content = content;
        this.fileType = fileType;
    }

    // Getters and Setters
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "GeneratedFile{" +
                "filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}