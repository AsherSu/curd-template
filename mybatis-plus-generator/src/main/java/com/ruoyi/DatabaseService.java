package com.ruoyi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseService {

    public static Connection openActiveConnection() throws Exception {
        Optional<DbConnectionInfo> opt = ConnectionRepository.getActive();
        if (!opt.isPresent()) throw new IllegalStateException("未设置活动数据库连接");
        DbConnectionInfo c = opt.get();
        Class.forName(c.getDriverClassName());
        return DriverManager.getConnection(c.getUrl(), c.getUsername(), c.getPassword());
    }

    public static Connection openConnection(DbConnectionInfo c) throws Exception {
        Class.forName(c.getDriverClassName());
        return DriverManager.getConnection(c.getUrl(), c.getUsername(), c.getPassword());
    }

    public static boolean test(DbConnectionInfo info) {
        try {
            Class.forName(info.getDriverClassName());
            try (Connection conn = DriverManager.getConnection(info.getUrl(), info.getUsername(), info.getPassword())) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static List<String> listSchemas() {
        List<String> result = new ArrayList<>();
        try (Connection conn = openActiveConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getSchemas()) {
                while (rs.next()) {
                    result.add(rs.getString("TABLE_SCHEM"));
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    public static List<String> listSchemas(DbConnectionInfo connInfo) {
        List<String> result = new ArrayList<>();
        try (Connection conn = openConnection(connInfo)) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getSchemas()) {
                while (rs.next()) {
                    result.add(rs.getString("TABLE_SCHEM"));
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    public static List<String> listTables(String schemaOrDb) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = openActiveConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(schemaOrDb, null, null, new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (Exception ignored) {}
        return tables;
    }

    public static List<String> listTables(DbConnectionInfo connInfo, String schemaOrDb) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = openConnection(connInfo)) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(schemaOrDb, null, null, new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (Exception ignored) {}
        return tables;
    }
} 