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
        try (Connection conn = openActiveConnection()) {
            return listSchemas(conn);
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    public static List<String> listSchemas(DbConnectionInfo connInfo) {
        try (Connection conn = openConnection(connInfo)) {
            return listSchemas(conn);
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    private static List<String> listSchemas(Connection conn) throws Exception {
        List<String> result = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        // 优先用 getSchemas
        try (ResultSet rs = meta.getSchemas()) {
            while (rs.next()) {
                String schema = safeGet(rs, "TABLE_SCHEM");
                if (schema != null && !schema.isEmpty()) result.add(schema);
            }
        }
        // 对于部分驱动/模式，可能需要补充 catalogs
        if (result.isEmpty()) {
            try (ResultSet rs = meta.getCatalogs()) {
                while (rs.next()) {
                    String catalog = safeGet(rs, "TABLE_CAT");
                    if (catalog != null && !catalog.isEmpty()) result.add(catalog);
                }
            }
        }
        return result;
    }

    public static List<String> listTables(String schemaOrDb) {
        try (Connection conn = openActiveConnection()) {
            return listTables(conn, schemaOrDb);
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    public static List<String> listTables(DbConnectionInfo connInfo, String schemaOrDb) {
        try (Connection conn = openConnection(connInfo)) {
            return listTables(conn, schemaOrDb);
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    private static List<String> listTables(Connection conn, String schemaOrDb) throws Exception {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        // 对于不同驱动，catalog/schema 的位置不同。尝试两种方式。
        // 方式一：schema 参数
        try (ResultSet rs = meta.getTables(null, schemaOrDb, null, new String[]{"TABLE"})) {
            while (rs.next()) tables.add(safeGet(rs, "TABLE_NAME"));
        }
        if (tables.isEmpty()) {
            // 方式二：catalog 参数
            try (ResultSet rs = meta.getTables(schemaOrDb, null, null, new String[]{"TABLE"})) {
                while (rs.next()) tables.add(safeGet(rs, "TABLE_NAME"));
            }
        }
        return tables;
    }

    private static String safeGet(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (Exception e) { return null; }
    }
} 