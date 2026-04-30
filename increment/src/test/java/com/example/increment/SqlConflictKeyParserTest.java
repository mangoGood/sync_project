package com.example.increment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SqlConflictKeyParserTest {

    private final SqlConflictKeyParser parser = new SqlConflictKeyParser();

    @Test
    void testParseInsert() {
        String sql = "INSERT INTO `mydb`.`users` (`id`, `name`, `age`) VALUES (1, 'Alice', 30);";
        SqlStatement stmt = parser.parse(sql, 1);

        assertNotNull(stmt);
        assertEquals(SqlStatement.OperationType.INSERT, stmt.getOperationType());
        assertEquals("mydb", stmt.getDatabase());
        assertEquals("users", stmt.getTableName());
        assertEquals("1", stmt.getPrimaryKeyValue());
        assertEquals("mydb.users:1", stmt.getConflictKey());
    }

    @Test
    void testParseUpdate() {
        String sql = "UPDATE `mydb`.`users` SET `name`='Bob' WHERE `id`=1;";
        SqlStatement stmt = parser.parse(sql, 2);

        assertNotNull(stmt);
        assertEquals(SqlStatement.OperationType.UPDATE, stmt.getOperationType());
        assertEquals("mydb", stmt.getDatabase());
        assertEquals("users", stmt.getTableName());
        assertEquals("1", stmt.getPrimaryKeyValue());
        assertEquals("mydb.users:1", stmt.getConflictKey());
    }

    @Test
    void testParseDelete() {
        String sql = "DELETE FROM `mydb`.`users` WHERE `id`=5;";
        SqlStatement stmt = parser.parse(sql, 3);

        assertNotNull(stmt);
        assertEquals(SqlStatement.OperationType.DELETE, stmt.getOperationType());
        assertEquals("mydb", stmt.getDatabase());
        assertEquals("users", stmt.getTableName());
        assertEquals("5", stmt.getPrimaryKeyValue());
    }

    @Test
    void testParseCommit() {
        String sql = "COMMIT;";
        SqlStatement stmt = parser.parse(sql, 4);

        assertNotNull(stmt);
        assertEquals(SqlStatement.OperationType.COMMIT, stmt.getOperationType());
        assertTrue(stmt.isTransactionBoundary());
    }

    @Test
    void testParseDDL() {
        String sql = "CREATE TABLE `mydb`.`test` (id INT PRIMARY KEY);";
        SqlStatement stmt = parser.parse(sql, 5);

        assertNotNull(stmt);
        assertEquals(SqlStatement.OperationType.DDL, stmt.getOperationType());
        assertTrue(stmt.isTransactionBoundary());
    }

    @Test
    void testDifferentTablesDifferentConflictKeys() {
        String sql1 = "INSERT INTO `mydb`.`users` (`id`) VALUES (1);";
        String sql2 = "INSERT INTO `mydb`.`orders` (`id`) VALUES (1);";

        SqlStatement stmt1 = parser.parse(sql1, 1);
        SqlStatement stmt2 = parser.parse(sql2, 2);

        assertNotEquals(stmt1.getConflictKey(), stmt2.getConflictKey());
    }

    @Test
    void testSameTableDifferentPkDifferentConflictKeys() {
        String sql1 = "INSERT INTO `mydb`.`users` (`id`) VALUES (1);";
        String sql2 = "INSERT INTO `mydb`.`users` (`id`) VALUES (2);";

        SqlStatement stmt1 = parser.parse(sql1, 1);
        SqlStatement stmt2 = parser.parse(sql2, 2);

        assertNotEquals(stmt1.getConflictKey(), stmt2.getConflictKey());
    }

    @Test
    void testSameTableSamePkSameConflictKey() {
        String sql1 = "INSERT INTO `mydb`.`users` (`id`, `name`) VALUES (1, 'Alice');";
        String sql2 = "UPDATE `mydb`.`users` SET `name`='Bob' WHERE `id`=1;";

        SqlStatement stmt1 = parser.parse(sql1, 1);
        SqlStatement stmt2 = parser.parse(sql2, 2);

        assertEquals(stmt1.getConflictKey(), stmt2.getConflictKey());
    }

    @Test
    void testInsertWithStringPrimaryKey() {
        String sql = "INSERT INTO `mydb`.`products` (`id`, `name`) VALUES ('P001', 'Widget');";
        SqlStatement stmt = parser.parse(sql, 1);

        assertNotNull(stmt);
        assertEquals("P001", stmt.getPrimaryKeyValue());
    }

    @Test
    void testCustomPrimaryKey() {
        SqlConflictKeyParser customParser = new SqlConflictKeyParser();
        customParser.registerPrimaryKey("orders", "order_id");

        String sql = "UPDATE `mydb`.`orders` SET `status`='shipped' WHERE `order_id`=100;";
        SqlStatement stmt = customParser.parse(sql, 1);

        assertNotNull(stmt);
        assertEquals("100", stmt.getPrimaryKeyValue());
        assertEquals("mydb.orders:100", stmt.getConflictKey());
    }
}
