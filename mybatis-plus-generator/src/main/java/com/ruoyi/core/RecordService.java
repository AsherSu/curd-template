package com.ruoyi.core;

import com.ruoyi.GeneratedFile;
import com.ruoyi.GenerationRecord;
import com.ruoyi.GenerationRecordManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecordService {

    public static class RestoreReport {
        private int restoredCount;
        private int skippedCount;
        private int overwrittenCount;

        public int getRestoredCount() { return restoredCount; }
        public int getSkippedCount() { return skippedCount; }
        public int getOverwrittenCount() { return overwrittenCount; }
        private void incRestored() { restoredCount++; }
        private void incSkipped() { skippedCount++; }
        private void incOverwritten() { overwrittenCount++; }

        @Override
        public String toString() {
            return String.format("恢复完成:%n  新建文件: %d%n  覆盖文件: %d%n  跳过文件: %d", restoredCount, overwrittenCount, skippedCount);
        }
    }

    public void save(GenerationRecord record) {
        GenerationRecordManager.saveRecord(record);
    }

    public void update(GenerationRecord record) {
        GenerationRecordManager.updateRecord(record);
    }

    public void deleteFilesOfRecord(GenerationRecord record) {
        if (record == null || record.getGeneratedFiles() == null) return;
        for (GeneratedFile gf : record.getGeneratedFiles()) {
            if (gf == null || gf.getFilePath() == null) continue;
            try {
                Path p = Paths.get(gf.getFilePath());
                if (Files.exists(p)) Files.delete(p);
            } catch (IOException ignored) {}
        }
    }

    public RestoreReport restore(GenerationRecord record, boolean overwriteOnDiff) {
        RestoreReport report = new RestoreReport();
        if (record == null || record.getGeneratedFiles() == null) return report;
        for (GeneratedFile gf : record.getGeneratedFiles()) {
            if (gf == null) continue;
            String filePath = gf.getFilePath();
            String content = gf.getContent();
            try {
                Path p = Paths.get(filePath);
                Path parent = p.getParent();
                if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
                if (!Files.exists(p)) {
                    Files.write(p, content == null ? new byte[0] : content.getBytes());
                    report.incRestored();
                    continue;
                }
                String existing = new String(Files.readAllBytes(p));
                if (existing.equals(content)) {
                    report.incSkipped();
                } else if (overwriteOnDiff) {
                    Files.write(p, content == null ? new byte[0] : content.getBytes());
                    report.incOverwritten();
                } else {
                    report.incSkipped();
                }
            } catch (Exception ignored) {}
        }
        return report;
    }
} 