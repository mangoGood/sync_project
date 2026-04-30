package com.example.increment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class ConcurrentExecutionIntegrationTest {

    private static final String SOURCE_URL = "jdbc:mysql://192.168.107.6:3306/?useSSL=false&serverTimezone=UTC";
    private static final String TARGET_URL = "jdbc:mysql://192.168.107.7:3306/?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD_SOURCE = "rootpassword";
    private static final String PASSWORD_TARGET = "rootpassword";
    private static final String DB_NAME = "concurrent_test_db";

    @Test
    void testDependencyGraphCorrectness() {
        List<SqlStatement> statements = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            statements.add(new SqlStatement.Builder().id(i - 1).seqno(i)
                    .sql("INSERT INTO " + DB_NAME + ".t1 VALUES (" + i + ")")
                    .operationType(SqlStatement.OperationType.INSERT)
                    .database(DB_NAME).tableName("t1").primaryKeyValue(String.valueOf(i)).build());
        }

        for (int i = 1; i <= 50; i++) {
            statements.add(new SqlStatement.Builder().id(100 + i - 1).seqno(100 + i)
                    .sql("INSERT INTO " + DB_NAME + ".t2 VALUES (" + i + ")")
                    .operationType(SqlStatement.OperationType.INSERT)
                    .database(DB_NAME).tableName("t2").primaryKeyValue(String.valueOf(i)).build());
        }

        for (int i = 1; i <= 10; i++) {
            statements.add(new SqlStatement.Builder().id(150 + i - 1).seqno(150 + i)
                    .sql("UPDATE " + DB_NAME + ".t1 SET val=" + (i + 100) + " WHERE id=" + i)
                    .operationType(SqlStatement.OperationType.UPDATE)
                    .database(DB_NAME).tableName("t1").primaryKeyValue(String.valueOf(i)).build());
        }

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(160, statements.size());
        assertEquals(160, layers.stream().mapToInt(List::size).sum());

        Map<Integer, Integer> stmtToLayer = new HashMap<>();
        for (int i = 0; i < layers.size(); i++) {
            for (SqlStatement s : layers.get(i)) {
                stmtToLayer.put(s.getId(), i);
            }
        }

        for (int i = 0; i < 100; i++) {
            Integer insertLayer = stmtToLayer.get(i);
            assertNotNull(insertLayer, "INSERT t1 id=" + (i + 1) + " should be in a layer");
        }

        for (int i = 0; i < 10; i++) {
            int insertId = i;
            int updateId = 150 + i;
            Integer insertLayer = stmtToLayer.get(insertId);
            Integer updateLayer = stmtToLayer.get(updateId);
            assertNotNull(insertLayer);
            assertNotNull(updateLayer);
            assertTrue(updateLayer > insertLayer,
                    "UPDATE on t1 id=" + (i + 1) + " must be after INSERT");
        }
    }

    @Test
    void testConcurrencyLevelMeasurement() {
        List<SqlStatement> statements = new ArrayList<>();

        for (int table = 1; table <= 5; table++) {
            for (int pk = 1; pk <= 100; pk++) {
                statements.add(new SqlStatement.Builder()
                        .id(statements.size()).seqno(statements.size() + 1)
                        .sql("INSERT INTO db.t" + table + " VALUES (" + pk + ")")
                        .operationType(SqlStatement.OperationType.INSERT)
                        .database("db").tableName("t" + table)
                        .primaryKeyValue(String.valueOf(pk)).build());
            }
        }

        for (int pk = 1; pk <= 100; pk++) {
            statements.add(new SqlStatement.Builder()
                    .id(statements.size()).seqno(statements.size() + 1)
                    .sql("UPDATE db.t1 SET val=val+1 WHERE id=" + pk)
                    .operationType(SqlStatement.OperationType.UPDATE)
                    .database("db").tableName("t1")
                    .primaryKeyValue(String.valueOf(pk)).build());
        }

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertTrue(layers.size() >= 2, "Should have at least 2 layers due to same-key dependencies");

        double avgConcurrency = graph.getAverageConcurrency();
        assertTrue(avgConcurrency > 1, "Average concurrency should be > 1 for diverse data, got: " + avgConcurrency);
    }

    @Test
    void testSequentialOrderPreservation() {
        List<SqlStatement> statements = new ArrayList<>();

        String[] operations = {"INSERT", "UPDATE", "UPDATE", "DELETE", "INSERT"};
        for (int i = 0; i < operations.length; i++) {
            statements.add(new SqlStatement.Builder().id(i).seqno(i + 1)
                    .sql(operations[i] + " INTO db.t1 VALUES (1)")
                    .operationType(SqlStatement.OperationType.valueOf(
                            operations[i].equals("INSERT") ? "INSERT" :
                                    operations[i].equals("UPDATE") ? "UPDATE" : "DELETE"))
                    .database("db").tableName("t1").primaryKeyValue("1").build());
        }

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(5, layers.size(), "5 operations on same PK should be in 5 layers");

        assertEquals(0, layers.get(0).get(0).getId());
        assertEquals(1, layers.get(1).get(0).getId());
        assertEquals(2, layers.get(2).get(0).getId());
        assertEquals(3, layers.get(3).get(0).getId());
        assertEquals(4, layers.get(4).get(0).getId());
    }

    @Test
    void testRealWorldScenario() {
        List<SqlStatement> statements = new ArrayList<>();
        int id = 0;

        for (int i = 1; i <= 5000; i++) {
            statements.add(new SqlStatement.Builder().id(id++).seqno(id)
                    .sql("INSERT INTO db.mysql_types_test VALUES (" + i + ")")
                    .operationType(SqlStatement.OperationType.INSERT)
                    .database("db").tableName("mysql_types_test")
                    .primaryKeyValue(String.valueOf(i)).build());
        }

        for (int i = 1; i <= 5000; i++) {
            statements.add(new SqlStatement.Builder().id(id++).seqno(id)
                    .sql("UPDATE db.mysql_types_test SET val=val+1 WHERE id=" + i)
                    .operationType(SqlStatement.OperationType.UPDATE)
                    .database("db").tableName("mysql_types_test")
                    .primaryKeyValue(String.valueOf(i)).build());
        }

        statements.add(new SqlStatement.Builder().id(id++).seqno(id)
                .sql("COMMIT")
                .operationType(SqlStatement.OperationType.COMMIT)
                .isTransactionBoundary(true).build());

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(10001, statements.size());
        assertEquals(10001, layers.stream().mapToInt(List::size).sum());

        Map<Integer, Integer> stmtToLayer = new HashMap<>();
        for (int i = 0; i < layers.size(); i++) {
            for (SqlStatement s : layers.get(i)) {
                stmtToLayer.put(s.getId(), i);
            }
        }

        for (int i = 0; i < 5000; i++) {
            int insertId = i;
            int updateId = 5000 + i;
            Integer insertLayer = stmtToLayer.get(insertId);
            Integer updateLayer = stmtToLayer.get(updateId);
            assertNotNull(insertLayer);
            assertNotNull(updateLayer);
            assertTrue(updateLayer > insertLayer,
                    "UPDATE for id=" + (i + 1) + " must be after INSERT");
        }

        long concurrentInserts = layers.get(0).stream()
                .filter(s -> s.getOperationType() == SqlStatement.OperationType.INSERT)
                .count();
        assertTrue(concurrentInserts >= 5000,
                "All 5000 INSERTs with different PKs should be concurrent in layer 0");

        logger.info("Real world scenario: {} statements, {} layers, {} concurrent inserts in first layer",
                statements.size(), layers.size(), concurrentInserts);
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConcurrentExecutionIntegrationTest.class);
}
