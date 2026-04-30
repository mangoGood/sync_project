package com.migration.full.migration;

import com.migration.db.DatabaseConnection;
import com.migration.model.ColumnInfo;
import com.migration.model.TableInfo;
import com.migration.model.TypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchemaMigration {
    private static final Logger logger = LoggerFactory.getLogger(SchemaMigration.class);

    private DatabaseConnection sourceConnection;
    private DatabaseConnection targetConnection;
    private boolean dropTables;
    private boolean isPostgresql;
    private boolean sourceIsPostgresql;
    private boolean targetIsPostgresql;

    public SchemaMigration(DatabaseConnection sourceConnection, DatabaseConnection targetConnection, boolean dropTables) {
        this.sourceConnection = sourceConnection;
        this.targetConnection = targetConnection;
        this.dropTables = dropTables;
        this.sourceIsPostgresql = "postgresql".equalsIgnoreCase(sourceConnection.getConfig().getDbType());
        this.targetIsPostgresql = "postgresql".equalsIgnoreCase(targetConnection.getConfig().getDbType());
        this.isPostgresql = targetIsPostgresql;
    }

    public void migrateAllTables(List<TableInfo> tables) throws SQLException {
        logger.info("开始迁移表结构，共 {} 个表", tables.size());

        int successCount = 0;
        int failCount = 0;

        for (TableInfo table : tables) {
            try {
                migrateTable(table);
                successCount++;
                logger.info("表 {} 结构迁移成功", table.getTableName());
            } catch (SQLException e) {
                failCount++;
                logger.error("表 {} 结构迁移失败，已忽略该错误继续执行", table.getTableName(), e);
            }
        }

        logger.info("表结构迁移完成，成功: {}, 失败: {}", successCount, failCount);
    }

    public void migrateTable(TableInfo table) throws SQLException {
        String tableName = table.getTableName();

        if (dropTables) {
            dropTableIfExists(tableName);
        }

        createTable(table);
    }

    private void dropTableIfExists(String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + quoteIdentifier(tableName);
        targetConnection.execute(sql);
        logger.debug("已删除表: {}", tableName);
    }

    private void createTable(TableInfo table) throws SQLException {
        if (sourceIsPostgresql && !targetIsPostgresql) {
            createTableFromPgToMysql(table);
            return;
        }
        if (!sourceIsPostgresql && targetIsPostgresql) {
            createTableFromMysqlToPg(table);
            return;
        }

        String createSql = table.getCreateSql();
        createSql = cleanCreateSql(createSql);
        targetConnection.execute(createSql);
        logger.debug("已创建表: {}", table.getTableName());
    }

    private void createTableFromPgToMysql(TableInfo table) throws SQLException {
        String createSql = generateMysqlCreateSqlFromPg(table);
        logger.debug("PG->MySQL 生成建表SQL: {}", createSql);
        targetConnection.execute(createSql);
        logger.debug("已创建表: {}", table.getTableName());
    }

    private void createTableFromMysqlToPg(TableInfo table) throws SQLException {
        String createSql = generatePgCreateSqlFromMysql(table);
        logger.debug("MySQL->PG 生成建表SQL: {}", createSql);
        targetConnection.execute(createSql);
        logger.debug("已创建表: {}", table.getTableName());
    }

    private String generateMysqlCreateSqlFromPg(TableInfo table) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(quoteIdentifier(table.getTableName())).append(" (\n");

        List<String> columnDefs = new ArrayList<>();
        List<String> pkColumns = new ArrayList<>();

        for (ColumnInfo col : table.getColumns()) {
            StringBuilder colDef = new StringBuilder();
            colDef.append("  ").append(quoteIdentifier(col.getColumnName())).append(" ");

            String mysqlType = TypeMapper.mapPgToMysqlColumnDef(col);
            colDef.append(mysqlType);

            columnDefs.add(colDef.toString());

            if (col.isPrimaryKey()) {
                pkColumns.add(col.getColumnName());
            }
        }

        if (!pkColumns.isEmpty()) {
            StringBuilder pkDef = new StringBuilder();
            pkDef.append("  PRIMARY KEY (");
            for (int i = 0; i < pkColumns.size(); i++) {
                if (i > 0) pkDef.append(", ");
                pkDef.append(quoteIdentifier(pkColumns.get(i)));
            }
            pkDef.append(")");
            columnDefs.add(pkDef.toString());
        }

        sb.append(String.join(",\n", columnDefs));
        sb.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        return sb.toString();
    }

    private String generatePgCreateSqlFromMysql(TableInfo table) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(quoteIdentifier(table.getTableName())).append(" (\n");

        List<String> columnDefs = new ArrayList<>();
        List<String> pkColumns = new ArrayList<>();

        for (ColumnInfo col : table.getColumns()) {
            StringBuilder colDef = new StringBuilder();
            colDef.append("  ").append(quoteIdentifier(col.getColumnName())).append(" ");

            String pgType = TypeMapper.mapMysqlToPgColumnDef(col);
            colDef.append(pgType);

            columnDefs.add(colDef.toString());

            if (col.isPrimaryKey()) {
                pkColumns.add(col.getColumnName());
            }
        }

        if (!pkColumns.isEmpty()) {
            StringBuilder pkDef = new StringBuilder();
            pkDef.append("  PRIMARY KEY (");
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

    private String cleanCreateSql(String createSql) {
        if (isPostgresql) {
            createSql = createSql.replaceAll("\"[^\"]+\"\\.\"", "\"");
            createSql = createSql.replaceAll("`[^`]+`\\.`", "\"");
            createSql = createSql.replaceAll("`", "\"");
            createSql = createSql.replaceAll("ENGINE\\s*=\\s*\\S+", "");
            createSql = createSql.replaceAll("DEFAULT\\s+CHARSET\\s*=\\s*\\S+", "");
            createSql = createSql.replaceAll("COLLATE\\s*=\\s*\\S+", "");
            createSql = createSql.replaceAll("AUTO_INCREMENT\\s*=\\s*\\d+", "");
            createSql = createSql.replaceAll(",\\s*,", ",");
            createSql = createSql.replaceAll("\\(\\s*,", "(");
            createSql = createSql.replaceAll(",\\s*\\)", ")");
            return createSql;
        }

        createSql = createSql.replaceAll("`[^`]+`\\.`", "`");
        createSql = createSql.replaceAll("AUTO_INCREMENT=\\d+", "AUTO_INCREMENT=1");

        return createSql;
    }

    public boolean tableExists(String tableName) throws SQLException {
        if (isPostgresql) {
            String schema = targetConnection.getConfig().getSchema();
            if (schema == null || schema.isEmpty()) {
                schema = "public";
            }
            String sql = "SELECT COUNT(*) FROM pg_tables WHERE schemaname = '" + schema + "' AND tablename = '" + tableName + "'";
            try (var stmt = targetConnection.getConnection().createStatement();
                 var rs = stmt.executeQuery(sql)) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }

        String sql = "SHOW TABLES LIKE '" + tableName + "'";
        try (var stmt = targetConnection.getConnection().createStatement();
                 var rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    private String quoteIdentifier(String identifier) {
        if (isPostgresql) {
            return "\"" + identifier + "\"";
        }
        return "`" + identifier + "`";
    }
}
