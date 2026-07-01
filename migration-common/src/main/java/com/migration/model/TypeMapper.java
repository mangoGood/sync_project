package com.migration.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class TypeMapper {

    private static final Map<String, String> PG_TO_MYSQL_TYPE_MAP = new LinkedHashMap<>();
    private static final Map<String, String> MYSQL_TO_PG_TYPE_MAP = new LinkedHashMap<>();

    static {
        PG_TO_MYSQL_TYPE_MAP.put("smallint", "SMALLINT");
        PG_TO_MYSQL_TYPE_MAP.put("int2", "SMALLINT");
        PG_TO_MYSQL_TYPE_MAP.put("integer", "INT");
        PG_TO_MYSQL_TYPE_MAP.put("int", "INT");
        PG_TO_MYSQL_TYPE_MAP.put("int4", "INT");
        PG_TO_MYSQL_TYPE_MAP.put("bigint", "BIGINT");
        PG_TO_MYSQL_TYPE_MAP.put("int8", "BIGINT");
        PG_TO_MYSQL_TYPE_MAP.put("oid", "BIGINT");
        PG_TO_MYSQL_TYPE_MAP.put("serial", "INT");
        PG_TO_MYSQL_TYPE_MAP.put("bigserial", "BIGINT");
        PG_TO_MYSQL_TYPE_MAP.put("real", "FLOAT");
        PG_TO_MYSQL_TYPE_MAP.put("float4", "FLOAT");
        PG_TO_MYSQL_TYPE_MAP.put("double precision", "DOUBLE");
        PG_TO_MYSQL_TYPE_MAP.put("float8", "DOUBLE");
        PG_TO_MYSQL_TYPE_MAP.put("money", "DECIMAL(19,2)");
        PG_TO_MYSQL_TYPE_MAP.put("character", "CHAR");
        PG_TO_MYSQL_TYPE_MAP.put("char", "CHAR");
        PG_TO_MYSQL_TYPE_MAP.put("character varying", "VARCHAR");
        PG_TO_MYSQL_TYPE_MAP.put("varchar", "VARCHAR");
        PG_TO_MYSQL_TYPE_MAP.put("text", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("boolean", "TINYINT(1)");
        PG_TO_MYSQL_TYPE_MAP.put("bool", "TINYINT(1)");
        PG_TO_MYSQL_TYPE_MAP.put("date", "DATE");
        PG_TO_MYSQL_TYPE_MAP.put("time without time zone", "TIME");
        PG_TO_MYSQL_TYPE_MAP.put("time", "TIME");
        PG_TO_MYSQL_TYPE_MAP.put("time with time zone", "TIME");
        PG_TO_MYSQL_TYPE_MAP.put("timetz", "TIME");
        PG_TO_MYSQL_TYPE_MAP.put("timestamp without time zone", "DATETIME");
        PG_TO_MYSQL_TYPE_MAP.put("timestamp", "DATETIME");
        PG_TO_MYSQL_TYPE_MAP.put("timestamp with time zone", "DATETIME");
        PG_TO_MYSQL_TYPE_MAP.put("timestamptz", "DATETIME");
        PG_TO_MYSQL_TYPE_MAP.put("interval", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("uuid", "CHAR(36)");
        PG_TO_MYSQL_TYPE_MAP.put("json", "JSON");
        PG_TO_MYSQL_TYPE_MAP.put("jsonb", "JSON");
        PG_TO_MYSQL_TYPE_MAP.put("bytea", "BLOB");
        PG_TO_MYSQL_TYPE_MAP.put("bit", "BIT");
        PG_TO_MYSQL_TYPE_MAP.put("bit varying", "VARCHAR");
        PG_TO_MYSQL_TYPE_MAP.put("varbit", "VARCHAR");
        PG_TO_MYSQL_TYPE_MAP.put("macaddr", "CHAR(17)");
        PG_TO_MYSQL_TYPE_MAP.put("macaddr8", "CHAR(23)");
        PG_TO_MYSQL_TYPE_MAP.put("inet", "VARCHAR(45)");
        PG_TO_MYSQL_TYPE_MAP.put("cidr", "VARCHAR(45)");
        PG_TO_MYSQL_TYPE_MAP.put("point", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("line", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("lseg", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("box", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("path", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("polygon", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("circle", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("xml", "LONGTEXT");
        PG_TO_MYSQL_TYPE_MAP.put("name", "VARCHAR(64)");
        PG_TO_MYSQL_TYPE_MAP.put("tsvector", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("tsquery", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("hstore", "TEXT");
        PG_TO_MYSQL_TYPE_MAP.put("ltree", "TEXT");

        MYSQL_TO_PG_TYPE_MAP.put("tinyint", "SMALLINT");
        MYSQL_TO_PG_TYPE_MAP.put("smallint", "SMALLINT");
        MYSQL_TO_PG_TYPE_MAP.put("mediumint", "INTEGER");
        MYSQL_TO_PG_TYPE_MAP.put("int", "INTEGER");
        MYSQL_TO_PG_TYPE_MAP.put("integer", "INTEGER");
        MYSQL_TO_PG_TYPE_MAP.put("bigint", "BIGINT");
        MYSQL_TO_PG_TYPE_MAP.put("float", "REAL");
        MYSQL_TO_PG_TYPE_MAP.put("double", "DOUBLE PRECISION");
        MYSQL_TO_PG_TYPE_MAP.put("decimal", "NUMERIC");
        MYSQL_TO_PG_TYPE_MAP.put("numeric", "NUMERIC");
        MYSQL_TO_PG_TYPE_MAP.put("char", "CHAR");
        MYSQL_TO_PG_TYPE_MAP.put("varchar", "VARCHAR");
        MYSQL_TO_PG_TYPE_MAP.put("binary", "BYTEA");
        MYSQL_TO_PG_TYPE_MAP.put("varbinary", "BYTEA");
        MYSQL_TO_PG_TYPE_MAP.put("tinyblob", "BYTEA");
        MYSQL_TO_PG_TYPE_MAP.put("blob", "BYTEA");
        MYSQL_TO_PG_TYPE_MAP.put("mediumblob", "BYTEA");
        MYSQL_TO_PG_TYPE_MAP.put("longblob", "BYTEA");
        MYSQL_TO_PG_TYPE_MAP.put("tinytext", "TEXT");
        MYSQL_TO_PG_TYPE_MAP.put("text", "TEXT");
        MYSQL_TO_PG_TYPE_MAP.put("mediumtext", "TEXT");
        MYSQL_TO_PG_TYPE_MAP.put("longtext", "TEXT");
        MYSQL_TO_PG_TYPE_MAP.put("date", "DATE");
        MYSQL_TO_PG_TYPE_MAP.put("time", "TIME");
        MYSQL_TO_PG_TYPE_MAP.put("datetime", "TIMESTAMP");
        MYSQL_TO_PG_TYPE_MAP.put("timestamp", "TIMESTAMP");
        MYSQL_TO_PG_TYPE_MAP.put("year", "SMALLINT");
        MYSQL_TO_PG_TYPE_MAP.put("boolean", "BOOLEAN");
        MYSQL_TO_PG_TYPE_MAP.put("bool", "BOOLEAN");
        MYSQL_TO_PG_TYPE_MAP.put("bit", "BIT");
        MYSQL_TO_PG_TYPE_MAP.put("enum", "VARCHAR");
        MYSQL_TO_PG_TYPE_MAP.put("set", "VARCHAR");
        MYSQL_TO_PG_TYPE_MAP.put("json", "JSONB");
    }

    public static String mapPgToMysql(String pgType) {
        if (pgType == null) {
            return "TEXT";
        }
        String lowerType = pgType.toLowerCase().trim();

        if (lowerType.startsWith("numeric") || lowerType.startsWith("decimal")) {
            return null;
        }
        if (lowerType.startsWith("character varying") || lowerType.startsWith("varchar")) {
            return null;
        }
        if (lowerType.startsWith("character") || lowerType.startsWith("char")) {
            return null;
        }
        if (lowerType.startsWith("bit varying") || lowerType.startsWith("varbit")) {
            return null;
        }
        if (lowerType.startsWith("bit")) {
            return null;
        }

        if (lowerType.endsWith("[]")) {
            return "TEXT";
        }

        String mapped = PG_TO_MYSQL_TYPE_MAP.get(lowerType);
        if (mapped != null) {
            return mapped;
        }

        for (Map.Entry<String, String> entry : PG_TO_MYSQL_TYPE_MAP.entrySet()) {
            if (lowerType.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "TEXT";
    }

    public static String mapMysqlToPg(String mysqlType) {
        if (mysqlType == null) {
            return "TEXT";
        }
        String lowerType = mysqlType.toLowerCase().trim();

        if (lowerType.startsWith("tinyint")) {
            return "SMALLINT";
        }
        if (lowerType.startsWith("smallint")) {
            return "SMALLINT";
        }
        if (lowerType.startsWith("mediumint")) {
            return "INTEGER";
        }
        if (lowerType.startsWith("int") || lowerType.startsWith("integer")) {
            return "INTEGER";
        }
        if (lowerType.startsWith("bigint")) {
            return "BIGINT";
        }
        if (lowerType.startsWith("float")) {
            return "REAL";
        }
        if (lowerType.startsWith("double")) {
            return "DOUBLE PRECISION";
        }
        if (lowerType.startsWith("decimal") || lowerType.startsWith("numeric")) {
            return "NUMERIC";
        }
        if (lowerType.startsWith("varchar")) {
            return "VARCHAR";
        }
        if (lowerType.startsWith("char")) {
            return "CHAR";
        }
        if (lowerType.startsWith("text") || lowerType.startsWith("tinytext") ||
            lowerType.startsWith("mediumtext") || lowerType.startsWith("longtext")) {
            return "TEXT";
        }
        if (lowerType.startsWith("binary") || lowerType.startsWith("varbinary") ||
            lowerType.startsWith("blob") || lowerType.startsWith("tinyblob") ||
            lowerType.startsWith("mediumblob") || lowerType.startsWith("longblob")) {
            return "BYTEA";
        }
        if (lowerType.startsWith("datetime") || lowerType.startsWith("timestamp")) {
            return "TIMESTAMP";
        }
        if (lowerType.startsWith("date")) {
            return "DATE";
        }
        if (lowerType.startsWith("time")) {
            return "TIME";
        }
        if (lowerType.startsWith("year")) {
            return "SMALLINT";
        }
        if (lowerType.startsWith("boolean") || lowerType.startsWith("bool")) {
            return "BOOLEAN";
        }
        if (lowerType.startsWith("bit")) {
            return "BIT";
        }
        if (lowerType.startsWith("enum") || lowerType.startsWith("set")) {
            return "VARCHAR";
        }
        if (lowerType.startsWith("json")) {
            return "JSONB";
        }

        String mapped = MYSQL_TO_PG_TYPE_MAP.get(lowerType);
        if (mapped != null) {
            return mapped;
        }

        for (Map.Entry<String, String> entry : MYSQL_TO_PG_TYPE_MAP.entrySet()) {
            if (lowerType.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "TEXT";
    }

    public static String mapMysqlToPgColumnDef(ColumnInfo column) {
        String mysqlType = column.getDataType();
        if (mysqlType == null) {
            return "TEXT";
        }
        String lowerType = mysqlType.toLowerCase().trim();

        String pgType;
        boolean isAutoIncrement = column.isAutoIncrement();

        if (lowerType.startsWith("tinyint")) {
            if (lowerType.contains("unsigned")) {
                pgType = "SMALLINT";
            } else {
                pgType = "SMALLINT";
            }
        } else if (lowerType.startsWith("smallint")) {
            pgType = lowerType.contains("unsigned") ? "INTEGER" : "SMALLINT";
        } else if (lowerType.startsWith("mediumint")) {
            pgType = "INTEGER";
        } else if (lowerType.startsWith("int") || lowerType.startsWith("integer")) {
            pgType = lowerType.contains("unsigned") ? "BIGINT" : "INTEGER";
        } else if (lowerType.startsWith("bigint")) {
            pgType = "BIGINT";
        } else if (lowerType.startsWith("float")) {
            pgType = "REAL";
        } else if (lowerType.startsWith("double")) {
            pgType = "DOUBLE PRECISION";
        } else if (lowerType.startsWith("decimal") || lowerType.startsWith("numeric")) {
            int precision = column.getColumnSize();
            int scale = column.getDecimalDigits();
            if (scale > 0) {
                pgType = "NUMERIC(" + precision + "," + scale + ")";
            } else if (precision > 0) {
                pgType = "NUMERIC(" + precision + ")";
            } else {
                pgType = "NUMERIC";
            }
        } else if (lowerType.startsWith("varchar")) {
            int size = column.getColumnSize();
            pgType = size > 0 ? "VARCHAR(" + size + ")" : "VARCHAR(255)";
        } else if (lowerType.startsWith("char")) {
            int size = column.getColumnSize();
            pgType = size > 0 ? "CHAR(" + size + ")" : "CHAR(1)";
        } else if (lowerType.startsWith("text") || lowerType.startsWith("tinytext") ||
                   lowerType.startsWith("mediumtext") || lowerType.startsWith("longtext")) {
            pgType = "TEXT";
        } else if (lowerType.startsWith("binary") || lowerType.startsWith("varbinary") ||
                   lowerType.startsWith("blob") || lowerType.startsWith("tinyblob") ||
                   lowerType.startsWith("mediumblob") || lowerType.startsWith("longblob")) {
            pgType = "BYTEA";
        } else if (lowerType.startsWith("datetime") || lowerType.startsWith("timestamp")) {
            pgType = "TIMESTAMP";
        } else if (lowerType.startsWith("date")) {
            pgType = "DATE";
        } else if (lowerType.startsWith("time")) {
            pgType = "TIME";
        } else if (lowerType.startsWith("year")) {
            pgType = "SMALLINT";
        } else if (lowerType.startsWith("boolean") || lowerType.startsWith("bool")) {
            pgType = "BOOLEAN";
        } else if (lowerType.startsWith("bit")) {
            int size = column.getColumnSize();
            pgType = size > 1 ? "BIT(" + size + ")" : "BOOLEAN";
        } else if (lowerType.startsWith("enum") || lowerType.startsWith("set")) {
            pgType = "VARCHAR(255)";
        } else if (lowerType.startsWith("json")) {
            pgType = "JSONB";
        } else {
            pgType = mapMysqlToPg(mysqlType);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(pgType);

        if (!column.isNullable()) {
            sb.append(" NOT NULL");
        }

        if (isAutoIncrement) {
            // PG uses SERIAL or IDENTITY instead of AUTO_INCREMENT
        } else if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
            String defaultVal = column.getDefaultValue();
            if (defaultVal.equalsIgnoreCase("CURRENT_TIMESTAMP") || defaultVal.equalsIgnoreCase("NOW()")) {
                sb.append(" DEFAULT CURRENT_TIMESTAMP");
            } else if (defaultVal.equalsIgnoreCase("NULL")) {
                // skip
            } else if (defaultVal.startsWith("'") || defaultVal.matches("-?\\d+(\\.\\d+)?")) {
                sb.append(" DEFAULT ").append(defaultVal);
            } else {
                sb.append(" DEFAULT '").append(defaultVal).append("'");
            }
        }

        return sb.toString();
    }

    public static String mapPgToMysqlColumnDef(ColumnInfo column) {
        String pgType = column.getDataType();
        if (pgType == null) {
            return "TEXT";
        }
        String lowerType = pgType.toLowerCase().trim();

        String mysqlType;
        boolean isSerial = "serial".equals(lowerType) || "bigserial".equals(lowerType);

        if (lowerType.startsWith("numeric") || lowerType.startsWith("decimal")) {
            int precision = column.getColumnSize();
            int scale = column.getDecimalDigits();
            if (scale > 0) {
                mysqlType = "DECIMAL(" + precision + "," + scale + ")";
            } else if (precision > 0) {
                mysqlType = "DECIMAL(" + precision + ")";
            } else {
                mysqlType = "DECIMAL";
            }
        } else if (lowerType.startsWith("character varying") || lowerType.startsWith("varchar")) {
            int size = column.getColumnSize();
            mysqlType = size > 0 ? "VARCHAR(" + size + ")" : "VARCHAR(255)";
        } else if (lowerType.startsWith("character") || lowerType.startsWith("char")) {
            int size = column.getColumnSize();
            mysqlType = size > 0 ? "CHAR(" + size + ")" : "CHAR(1)";
        } else if (lowerType.startsWith("bit varying") || lowerType.startsWith("varbit")) {
            int size = column.getColumnSize();
            mysqlType = size > 0 ? "VARCHAR(" + size + ")" : "VARCHAR(255)";
        } else if (lowerType.startsWith("bit") && !lowerType.startsWith("bit varying")) {
            int size = column.getColumnSize();
            mysqlType = size > 0 ? "BIT(" + size + ")" : "BIT(1)";
        } else if (lowerType.endsWith("[]")) {
            mysqlType = "TEXT";
        } else {
            mysqlType = mapPgToMysql(pgType);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(mysqlType);

        if (isSerial) {
            column.setAutoIncrement(true);
        }

        if (!column.isNullable()) {
            sb.append(" NOT NULL");
        }

        if (column.isAutoIncrement()) {
            sb.append(" AUTO_INCREMENT");
        } else if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
            String defaultVal = column.getDefaultValue();
            if (isSerial) {
                // skip DEFAULT for serial columns
            } else if (defaultVal.contains("nextval(")) {
                // skip PG sequence defaults
            } else if (isPgTimestampFunction(defaultVal)) {
                sb.append(" DEFAULT ").append(convertPgTimestampDefault(defaultVal));
            } else if (defaultVal.startsWith("'") || defaultVal.matches("-?\\d+(\\.\\d+)?")) {
                sb.append(" DEFAULT ").append(defaultVal);
            } else if (defaultVal.equalsIgnoreCase("true")) {
                sb.append(" DEFAULT 1");
            } else if (defaultVal.equalsIgnoreCase("false")) {
                sb.append(" DEFAULT 0");
            } else {
                sb.append(" DEFAULT '").append(defaultVal).append("'");
            }
        }

        return sb.toString();
    }

    public static boolean isPgArrayType(String pgType) {
        return pgType != null && pgType.endsWith("[]");
    }

    public static boolean isPgBooleanType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "boolean".equals(lower) || "bool".equals(lower);
    }

    public static boolean isPgUuidType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "uuid".equals(lower);
    }

    public static boolean isPgJsonbType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "jsonb".equals(lower);
    }

    public static boolean isPgJsonType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "json".equals(lower);
    }

    public static boolean isPgTimestampTzType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "timestamp with time zone".equals(lower) || "timestamptz".equals(lower);
    }

    public static boolean isPgIntervalType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "interval".equals(lower);
    }

    public static boolean isPgNetworkType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "inet".equals(lower) || "cidr".equals(lower) || "macaddr".equals(lower) || "macaddr8".equals(lower);
    }

    public static boolean isPgTimetzType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "time with time zone".equals(lower) || "timetz".equals(lower);
    }

    public static boolean isPgGeometryType(String pgType) {
        if (pgType == null) return false;
        String lower = pgType.toLowerCase().trim();
        return "point".equals(lower) || "line".equals(lower) || "lseg".equals(lower) ||
               "box".equals(lower) || "path".equals(lower) || "polygon".equals(lower) || "circle".equals(lower);
    }

    public static boolean needsValueConversion(String pgType) {
        return isPgBooleanType(pgType) || isPgUuidType(pgType) || isPgArrayType(pgType) ||
               isPgJsonbType(pgType) || isPgTimestampTzType(pgType) || isPgIntervalType(pgType) ||
               isPgNetworkType(pgType) || isPgGeometryType(pgType);
    }

    private static boolean isPgTimestampFunction(String defaultVal) {
        if (defaultVal == null) return false;
        String lower = defaultVal.toLowerCase().trim();
        return lower.equals("now()") || lower.equals("current_timestamp") ||
               lower.equals("clock_timestamp()") || lower.equals("statement_timestamp()") ||
               lower.equals("transaction_timestamp()") || lower.startsWith("now(") ||
               lower.contains("timezone(") || lower.contains("date_trunc(");
    }

    private static String convertPgTimestampDefault(String defaultVal) {
        if (defaultVal == null) return "CURRENT_TIMESTAMP";
        String lower = defaultVal.toLowerCase().trim();
        if (lower.equals("now()") || lower.equals("current_timestamp") ||
            lower.equals("clock_timestamp()") || lower.equals("statement_timestamp()") ||
            lower.equals("transaction_timestamp()")) {
            return "CURRENT_TIMESTAMP";
        }
        return "CURRENT_TIMESTAMP";
    }
}
