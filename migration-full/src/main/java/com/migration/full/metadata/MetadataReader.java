package com.migration.full.metadata;

import com.migration.db.DatabaseConnection;
import com.migration.model.ColumnInfo;
import com.migration.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MetadataReader {
    private static final Logger logger = LoggerFactory.getLogger(MetadataReader.class);

    private DatabaseConnection connection;
    private boolean isPostgresql;

    public MetadataReader(DatabaseConnection connection) {
        this.connection = connection;
        this.isPostgresql = "postgresql".equalsIgnoreCase(connection.getConfig().getDbType());
    }

    public List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();

        if (isPostgresql) {
            String schema = connection.getConfig().getSchema();
            if (schema == null || schema.isEmpty()) {
                schema = "public";
            }
            String sql = "SELECT tablename FROM pg_tables WHERE schemaname = '" + schema + "'";
            try (Statement stmt = connection.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        } else {
            String sql = "SHOW TABLES";
            try (Statement stmt = connection.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        }

        logger.info("找到 {} 个表", tables.size());
        return tables;
    }

    public String getCreateTableSql(String tableName) throws SQLException {
        if (isPostgresql) {
            return generatePostgresCreateSql(tableName);
        }

        String sql = "SHOW CREATE TABLE " + quoteIdentifier(tableName);
        try (Statement stmt = connection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(2);
            }
        }

        return null;
    }

    private String generatePostgresCreateSql(String tableName) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(quoteIdentifier(tableName)).append(" (\n");

        String schema = connection.getConfig().getSchema();
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }

        DatabaseMetaData meta = connection.getConnection().getMetaData();
        List<String> columnDefs = new ArrayList<>();
        List<String> pkColumns = new ArrayList<>();

        try (ResultSet cols = meta.getColumns(null, schema, tableName, null)) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                String typeName = cols.getString("TYPE_NAME");
                int colSize = cols.getInt("COLUMN_SIZE");
                String isNullable = cols.getString("IS_NULLABLE");
                String defaultValue = cols.getString("COLUMN_DEF");
                String isAutoInc = cols.getString("IS_AUTOINCREMENT");

                StringBuilder colDef = new StringBuilder();
                colDef.append("  ").append(quoteIdentifier(colName)).append(" ").append(typeName);
                if ("serial".equalsIgnoreCase(typeName) || "bigserial".equalsIgnoreCase(typeName)) {
                    // no size
                } else if ("character varying".equalsIgnoreCase(typeName) || "varchar".equalsIgnoreCase(typeName)) {
                    colDef.append("(").append(colSize).append(")");
                } else if ("numeric".equalsIgnoreCase(typeName) || "decimal".equalsIgnoreCase(typeName)) {
                    int decimalDigits = cols.getInt("DECIMAL_DIGITS");
                    if (decimalDigits > 0) {
                        colDef.append("(").append(colSize).append(",").append(decimalDigits).append(")");
                    } else if (colSize > 0) {
                        colDef.append("(").append(colSize).append(")");
                    }
                }

                if ("NO".equalsIgnoreCase(isNullable)) {
                    colDef.append(" NOT NULL");
                }
                boolean isSerialType = "serial".equalsIgnoreCase(typeName) || "bigserial".equalsIgnoreCase(typeName);
                if (defaultValue != null && !defaultValue.isEmpty() && !isSerialType) {
                    colDef.append(" DEFAULT ").append(defaultValue);
                }

                columnDefs.add(colDef.toString());
            }
        }

        try (ResultSet pks = meta.getPrimaryKeys(null, schema, tableName)) {
            while (pks.next()) {
                pkColumns.add(pks.getString("COLUMN_NAME"));
            }
        }

        if (!pkColumns.isEmpty()) {
            StringBuilder pkDef = new StringBuilder();
            pkDef.append("  CONSTRAINT ").append(quoteIdentifier("pk_" + tableName)).append(" PRIMARY KEY (");
            for (int i = 0; i < pkColumns.size(); i++) {
                if (i > 0) pkDef.append(", ");
                pkDef.append(quoteIdentifier(pkColumns.get(i)));
            }
            pkDef.append(")");
            columnDefs.add(pkDef.toString());
        }

        sb.append(String.join(",\n", columnDefs));
        sb.append("\n)");

        return sb.toString();
    }

    public TableInfo getTableInfo(String tableName) throws SQLException {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(tableName);
        tableInfo.setCreateSql(getCreateTableSql(tableName));

        if (isPostgresql) {
            fillPostgresColumnInfo(tableName, tableInfo);
        } else {
            fillMysqlColumnInfo(tableName, tableInfo);
        }

        logger.debug("表 {} 的列信息: {}", tableName, tableInfo.getColumns().size());
        return tableInfo;
    }

    private void fillPostgresColumnInfo(String tableName, TableInfo tableInfo) throws SQLException {
        DatabaseMetaData meta = connection.getConnection().getMetaData();
        String schema = connection.getConfig().getSchema();
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }

        try (ResultSet cols = meta.getColumns(null, schema, tableName, null)) {
            while (cols.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setColumnName(cols.getString("COLUMN_NAME"));
                column.setDataType(cols.getString("TYPE_NAME"));
                column.setColumnSize(cols.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(cols.getInt("DECIMAL_DIGITS"));
                column.setNullable("YES".equalsIgnoreCase(cols.getString("IS_NULLABLE")));
                column.setDefaultValue(cols.getString("COLUMN_DEF"));
                String isAutoInc = cols.getString("IS_AUTOINCREMENT");
                column.setAutoIncrement("YES".equalsIgnoreCase(isAutoInc));
                tableInfo.addColumn(column);
            }
        }

        try (ResultSet pks = meta.getPrimaryKeys(null, schema, tableName)) {
            while (pks.next()) {
                String pkCol = pks.getString("COLUMN_NAME");
                for (ColumnInfo col : tableInfo.getColumns()) {
                    if (col.getColumnName().equals(pkCol)) {
                        col.setPrimaryKey(true);
                        break;
                    }
                }
            }
        }
    }

    private void fillMysqlColumnInfo(String tableName, TableInfo tableInfo) throws SQLException {
        String sql = "DESCRIBE " + quoteIdentifier(tableName);
        try (Statement stmt = connection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setColumnName(rs.getString("Field"));
                column.setDataType(rs.getString("Type"));
                column.setNullable("YES".equals(rs.getString("Null")));
                column.setDefaultValue(rs.getString("Default"));
                column.setAutoIncrement("auto_increment".equalsIgnoreCase(rs.getString("Extra")));
                tableInfo.addColumn(column);
            }
        }

        getPrimaryKeyInfoMysql(tableName, tableInfo);
    }

    private void getPrimaryKeyInfoMysql(String tableName, TableInfo tableInfo) throws SQLException {
        String sql = "SHOW KEYS FROM " + quoteIdentifier(tableName) + " WHERE Key_name = 'PRIMARY'";
        try (Statement stmt = connection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String columnName = rs.getString("Column_name");
                for (ColumnInfo column : tableInfo.getColumns()) {
                    if (column.getColumnName().equals(columnName)) {
                        column.setPrimaryKey(true);
                        break;
                    }
                }
            }
        }
    }

    public List<TableInfo> getAllTablesInfo() throws SQLException {
        List<TableInfo> tablesInfo = new ArrayList<>();
        List<String> tables = getAllTables();

        for (String tableName : tables) {
            try {
                TableInfo tableInfo = getTableInfo(tableName);
                tablesInfo.add(tableInfo);
            } catch (SQLException e) {
                logger.error("获取表 {} 的信息失败", tableName, e);
                throw e;
            }
        }

        return tablesInfo;
    }

    public long getTableRowCount(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);

        try (Statement stmt = connection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }

        return 0;
    }

    public List<TableInfo> getFilteredTablesInfo(Set<String> includedTables) throws SQLException {
        List<TableInfo> tablesInfo = new ArrayList<>();
        List<String> tables = getAllTables();

        for (String tableName : tables) {
            if (includedTables != null && !includedTables.isEmpty()) {
                boolean shouldInclude = false;
                for (String includedTable : includedTables) {
                    if (includedTable.equals(tableName) || includedTable.endsWith("." + tableName)) {
                        shouldInclude = true;
                        break;
                    }
                }
                if (!shouldInclude) {
                    logger.debug("表 {} 不在同步对象列表中，跳过", tableName);
                    continue;
                }
            }

            try {
                TableInfo tableInfo = getTableInfo(tableName);
                tablesInfo.add(tableInfo);
            } catch (SQLException e) {
                logger.error("获取表 {} 的信息失败", tableName, e);
                throw e;
            }
        }

        return tablesInfo;
    }

    public String quoteIdentifier(String identifier) {
        if (isPostgresql) {
            return "\"" + identifier + "\"";
        }
        return "`" + identifier + "`";
    }

    public boolean isPostgresql() {
        return isPostgresql;
    }
}
