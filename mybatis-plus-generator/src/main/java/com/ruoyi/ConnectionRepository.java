package com.ruoyi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionRepository {
    private static final String STORE_FILE = "db_connections.json";
    private static final String ACTIVE_FILE = "db_active_connection.txt";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final AtomicLong idGen = new AtomicLong(1);

    static { mapper.registerModule(new JavaTimeModule()); }

    public static synchronized List<DbConnectionInfo> findAll() {
        try {
            Path p = Paths.get(STORE_FILE);
            if (!Files.exists(p)) return new ArrayList<>();
            byte[] data = Files.readAllBytes(p);
            if (data.length == 0) return new ArrayList<>();
            DbConnectionInfo[] arr = mapper.readValue(data, DbConnectionInfo[].class);
            List<DbConnectionInfo> list = new ArrayList<>();
            for (DbConnectionInfo c : arr) list.add(c);
            list.sort(Comparator.comparing(DbConnectionInfo::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            // 恢复 id 生成器
            for (DbConnectionInfo c : list) {
                if (c.getId() != null && c.getId() >= idGen.get()) idGen.set(c.getId() + 1);
            }
            return list;
        } catch (Exception e) { return new ArrayList<>(); }
    }

    public static synchronized DbConnectionInfo save(DbConnectionInfo info) {
        List<DbConnectionInfo> list = findAll();
        if (info.getId() == null) info.setId(idGen.getAndIncrement());
        info.setUpdatedAt(LocalDateTime.now());
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(info.getId())) { list.set(i, info); replaced = true; break; }
        }
        if (!replaced) list.add(info);
        writeAll(list);
        return info;
    }

    public static synchronized void delete(long id) {
        List<DbConnectionInfo> list = findAll();
        list.removeIf(c -> c.getId() == id);
        writeAll(list);
        Long activeId = getActiveId();
        if (activeId != null && activeId == id) clearActive();
    }

    private static void writeAll(List<DbConnectionInfo> list) {
        try { Files.write(Paths.get(STORE_FILE), mapper.writeValueAsBytes(list)); } catch (IOException ignored) {}
    }

    public static synchronized void setActive(long id) {
        try { Files.write(Paths.get(ACTIVE_FILE), String.valueOf(id).getBytes()); } catch (IOException ignored) {}
    }

    public static synchronized void clearActive() {
        try { Files.deleteIfExists(Paths.get(ACTIVE_FILE)); } catch (IOException ignored) {}
    }

    public static synchronized Long getActiveId() {
        try {
            Path p = Paths.get(ACTIVE_FILE);
            if (!Files.exists(p)) return null;
            String s = new String(Files.readAllBytes(p)).trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception e) { return null; }
    }

    public static synchronized Optional<DbConnectionInfo> getActive() {
        Long id = getActiveId();
        if (id == null) return Optional.empty();
        return findAll().stream().filter(c -> id.equals(c.getId())).findFirst();
    }
} 