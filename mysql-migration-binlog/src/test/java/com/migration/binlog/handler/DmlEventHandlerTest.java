package com.migration.binlog.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.time.*;
import java.util.Calendar;

/**
 * DmlEventHandler formatValue 方法测试
 * 测试 MySQL 所有数据类型的 SQL 拼接正确性
 */
public class DmlEventHandlerTest {

    /**
     * 使用反射调用私有方法
     */
    private String formatValue(Serializable value) throws Exception {
        java.lang.reflect.Method method = DmlEventHandler.class.getDeclaredMethod("formatValue", Serializable.class);
        method.setAccessible(true);
        DmlEventHandler handler = new DmlEventHandler("/tmp/test");
        return (String) method.invoke(handler, value);
    }

    @Test
    @DisplayName("测试 NULL 值")
    public void testNullValue() throws Exception {
        assertEquals("NULL", formatValue(null));
    }

    @Test
    @DisplayName("测试整数类型 - TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT")
    public void testIntegerTypes() throws Exception {
        assertEquals("0", formatValue((byte) 0));
        assertEquals("127", formatValue((byte) 127));
        assertEquals("-128", formatValue((byte) -128));
        
        assertEquals("32767", formatValue((short) 32767));
        assertEquals("-32768", formatValue((short) -32768));
        
        assertEquals("2147483647", formatValue(2147483647));
        assertEquals("-2147483648", formatValue(-2147483648));
        
        assertEquals("9223372036854775807", formatValue(9223372036854775807L));
        assertEquals("-9223372036854775808", formatValue(-9223372036854775808L));
        
        assertEquals("12345678901234567890", formatValue(new BigInteger("12345678901234567890")));
    }

    @Test
    @DisplayName("测试浮点类型 - FLOAT, DOUBLE")
    public void testFloatTypes() throws Exception {
        assertEquals("3.14159", formatValue(3.14159f));
        assertEquals("2.718281828459045", formatValue(2.718281828459045));
        
        BigDecimal bd = new BigDecimal("123.456789012345678901234567890");
        assertEquals("123.456789012345678901234567890", formatValue(bd));
    }

    @Test
    @DisplayName("测试布尔类型 - BOOLEAN")
    public void testBooleanType() throws Exception {
        assertEquals("1", formatValue(true));
        assertEquals("0", formatValue(false));
    }

    @Test
    @DisplayName("测试时间类型 - TIMESTAMP - 关键测试")
    public void testTimestampType() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 10, 13, 59, 32);
        cal.set(Calendar.MILLISECOND, 0);
        
        Timestamp ts = new Timestamp(cal.getTimeInMillis());
        String result = formatValue(ts);
        
        assertEquals("'2026-03-10 13:59:32'", result, 
            "Timestamp 无毫秒时应格式化为 'yyyy-MM-dd HH:mm:ss'");
        
        cal.set(Calendar.MILLISECOND, 123);
        ts = new Timestamp(cal.getTimeInMillis());
        result = formatValue(ts);
        
        assertTrue(result.startsWith("'2026-03-10 13:59:32."),
            "Timestamp 有毫秒时应包含小数部分");
    }

    @Test
    @DisplayName("测试时间类型 - TIMESTAMP 带微秒")
    public void testTimestampWithMicroseconds() throws Exception {
        Timestamp ts = Timestamp.valueOf("2026-03-10 13:59:32.123456");
        String result = formatValue(ts);
        
        assertEquals("'2026-03-10 13:59:32.123456'", result,
            "Timestamp 微秒应正确格式化，末尾的 0 应被去掉");
    }

    @Test
    @DisplayName("测试时间类型 - TIMESTAMP 毫秒为 .0 的情况")
    public void testTimestampZeroMillis() throws Exception {
        Timestamp ts = Timestamp.valueOf("2026-03-10 13:59:32.0");
        String result = formatValue(ts);
        
        assertEquals("'2026-03-10 13:59:32'", result,
            "Timestamp 毫秒为 .0 时不应显示小数部分");
    }

    @Test
    @DisplayName("测试日期类型 - DATE")
    public void testDateType() throws Exception {
        Date date = Date.valueOf("2026-03-10");
        String result = formatValue(date);
        
        assertEquals("'2026-03-10'", result);
    }

    @Test
    @DisplayName("测试时间类型 - TIME")
    public void testTimeType() throws Exception {
        Time time = Time.valueOf("13:59:32");
        String result = formatValue(time);
        
        assertEquals("'13:59:32'", result);
    }

    @Test
    @DisplayName("测试 Java 8 时间类型 - LocalDateTime")
    public void testLocalDateTime() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2026, 3, 10, 13, 59, 32);
        String result = formatValue(ldt);
        
        assertEquals("'2026-03-10 13:59:32'", result);
        
        ldt = LocalDateTime.of(2026, 3, 10, 13, 59, 32, 123456789);
        result = formatValue(ldt);
        
        assertTrue(result.startsWith("'2026-03-10 13:59:32.123456"),
            "LocalDateTime 带纳秒应正确格式化");
    }

    @Test
    @DisplayName("测试 Java 8 时间类型 - LocalDate")
    public void testLocalDate() throws Exception {
        LocalDate ld = LocalDate.of(2026, 3, 10);
        String result = formatValue(ld);
        
        assertEquals("'2026-03-10'", result);
    }

    @Test
    @DisplayName("测试 Java 8 时间类型 - LocalTime")
    public void testLocalTime() throws Exception {
        LocalTime lt = LocalTime.of(13, 59, 32);
        String result = formatValue(lt);
        
        assertEquals("'13:59:32'", result);
    }

    @Test
    @DisplayName("测试字符串类型 - VARCHAR, CHAR, TEXT")
    public void testStringTypes() throws Exception {
        assertEquals("'hello world'", formatValue("hello world"));
        assertEquals("'测试中文'", formatValue("测试中文"));
        assertEquals("''", formatValue(""));
        
        assertEquals("'it\\'s a test'", formatValue("it's a test"),
            "单引号应被转义");
        
        assertEquals("'line1\\nline2'", formatValue("line1\nline2"),
            "换行符应被转义");
        
        assertEquals("'tab\\tseparated'", formatValue("tab\tseparated"),
            "制表符应被转义");
        
        assertEquals("'path\\\\to\\\\file'", formatValue("path\\to\\file"),
            "反斜杠应被转义");
        
        assertEquals("'carriage\\rreturn'", formatValue("carriage\rreturn"),
            "回车符应被转义");
    }

    @Test
    @DisplayName("测试二进制类型 - BINARY, VARBINARY, BLOB")
    public void testBinaryTypes() throws Exception {
        byte[] bytes = new byte[]{0x01, 0x02, 0x03, (byte) 0xFF};
        String result = formatValue(bytes);
        
        assertEquals("0x010203ff", result);
        
        bytes = new byte[]{0};
        result = formatValue(bytes);
        
        assertEquals("0x00", result);
    }

    @Test
    @DisplayName("测试 DECIMAL 类型精度")
    public void testDecimalPrecision() throws Exception {
        BigDecimal bd = new BigDecimal("9999999999.9999999999");
        String result = formatValue(bd);
        
        assertEquals("9999999999.9999999999", result,
            "高精度 DECIMAL 应保持精度");
        
        bd = new BigDecimal("0.0000000001");
        result = formatValue(bd);
        
        assertEquals("0.0000000001", result);
    }

    @Test
    @DisplayName("测试特殊数值")
    public void testSpecialNumbers() throws Exception {
        assertEquals("0", formatValue(0));
        assertEquals("0", formatValue(0L));
        assertEquals("0.0", formatValue(0.0));  // BigDecimal("0.0").toPlainString() = "0.0"
        assertEquals("0.0", formatValue(0.0f));  // Float 也走 BigDecimal 路径
        
        assertEquals("-1", formatValue(-1));
        assertEquals("-1", formatValue(-1L));
    }

    @Test
    @DisplayName("测试完整 DELETE SQL 生成")
    public void testDeleteSqlGeneration() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 10, 13, 59, 32);
        cal.set(Calendar.MILLISECOND, 0);
        
        Timestamp createdAt = new Timestamp(cal.getTimeInMillis());
        Timestamp updatedAt = new Timestamp(cal.getTimeInMillis());
        
        String formattedCreatedAt = formatValue(createdAt);
        String formattedUpdatedAt = formatValue(updatedAt);
        
        assertEquals("'2026-03-10 13:59:32'", formattedCreatedAt);
        assertEquals("'2026-03-10 13:59:32'", formattedUpdatedAt);
        
        String expectedWhere = "created_at = '2026-03-10 13:59:32' AND updated_at = '2026-03-10 13:59:32'";
        assertTrue(expectedWhere.contains(formattedCreatedAt));
        assertTrue(expectedWhere.contains(formattedUpdatedAt));
    }
}
