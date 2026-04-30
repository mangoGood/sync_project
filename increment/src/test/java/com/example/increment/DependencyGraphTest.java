package com.example.increment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class DependencyGraphTest {

    @Test
    void testDifferentTablesCanBeConcurrent() {
        List<SqlStatement> statements = new ArrayList<>();
        statements.add(new SqlStatement.Builder().id(0).seqno(1).sql("INSERT INTO db.t1 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(1).seqno(2).sql("INSERT INTO db.t2 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t2").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(2).seqno(3).sql("INSERT INTO db.t3 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t3").primaryKeyValue("1").build());

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(1, layers.size(), "Different tables should be in the same layer (concurrent)");
        assertEquals(3, layers.get(0).size(), "All 3 statements should be in the first layer");
    }

    @Test
    void testSameTableDifferentPkCanBeConcurrent() {
        List<SqlStatement> statements = new ArrayList<>();
        statements.add(new SqlStatement.Builder().id(0).seqno(1).sql("INSERT INTO db.t1 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(1).seqno(2).sql("INSERT INTO db.t1 VALUES (2)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("2").build());
        statements.add(new SqlStatement.Builder().id(2).seqno(3).sql("INSERT INTO db.t1 VALUES (3)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("3").build());

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(1, layers.size(), "Same table different PK should be in the same layer");
        assertEquals(3, layers.get(0).size());
    }

    @Test
    void testSameTableSamePkMustBeSequential() {
        List<SqlStatement> statements = new ArrayList<>();
        statements.add(new SqlStatement.Builder().id(0).seqno(1).sql("INSERT INTO db.t1 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(1).seqno(2).sql("UPDATE db.t1 SET x=2 WHERE id=1")
                .operationType(SqlStatement.OperationType.UPDATE).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(2).seqno(3).sql("DELETE FROM db.t1 WHERE id=1")
                .operationType(SqlStatement.OperationType.DELETE).database("db").tableName("t1").primaryKeyValue("1").build());

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(3, layers.size(), "Same table same PK must be in different layers");
        for (int i = 0; i < 3; i++) {
            assertEquals(1, layers.get(i).size(), "Each layer should have exactly 1 statement");
        }
    }

    @Test
    void testMixedConflictKeys() {
        List<SqlStatement> statements = new ArrayList<>();
        statements.add(new SqlStatement.Builder().id(0).seqno(1).sql("INSERT INTO db.t1 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(1).seqno(2).sql("INSERT INTO db.t1 VALUES (2)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("2").build());
        statements.add(new SqlStatement.Builder().id(2).seqno(3).sql("UPDATE db.t1 SET x=2 WHERE id=1")
                .operationType(SqlStatement.OperationType.UPDATE).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(3).seqno(4).sql("INSERT INTO db.t2 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t2").primaryKeyValue("1").build());

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertTrue(layers.size() >= 2, "Should have at least 2 layers");

        SqlStatement layer0stmt0 = layers.get(0).stream()
                .filter(s -> s.getId() == 0).findFirst().orElse(null);
        SqlStatement layer0stmt1 = layers.get(0).stream()
                .filter(s -> s.getId() == 1).findFirst().orElse(null);
        SqlStatement layer0stmt3 = layers.get(0).stream()
                .filter(s -> s.getId() == 3).findFirst().orElse(null);

        assertNotNull(layer0stmt0, "INSERT t1 id=1 should be in layer 0");
        assertNotNull(layer0stmt1, "INSERT t1 id=2 should be in layer 0");
        assertNotNull(layer0stmt3, "INSERT t2 id=1 should be in layer 0");

        SqlStatement updateStmt = layers.stream()
                .flatMap(List::stream)
                .filter(s -> s.getId() == 2).findFirst().orElse(null);
        assertNotNull(updateStmt);
        int updateLayer = -1;
        for (int i = 0; i < layers.size(); i++) {
            for (SqlStatement s : layers.get(i)) {
                if (s.getId() == 2) { updateLayer = i; break; }
            }
        }
        assertTrue(updateLayer > 0, "UPDATE on t1 id=1 must be after INSERT on t1 id=1");
    }

    @Test
    void testTransactionBoundaryActsAsBarrier() {
        List<SqlStatement> statements = new ArrayList<>();
        statements.add(new SqlStatement.Builder().id(0).seqno(1).sql("INSERT INTO db.t1 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(1).seqno(2).sql("INSERT INTO db.t2 VALUES (1)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t2").primaryKeyValue("1").build());
        statements.add(new SqlStatement.Builder().id(2).seqno(3).sql("COMMIT")
                .operationType(SqlStatement.OperationType.COMMIT).isTransactionBoundary(true).build());
        statements.add(new SqlStatement.Builder().id(3).seqno(4).sql("INSERT INTO db.t1 VALUES (2)")
                .operationType(SqlStatement.OperationType.INSERT).database("db").tableName("t1").primaryKeyValue("2").build());

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        int commitLayer = -1;
        int insert1Layer = -1;
        int insert2Layer = -1;
        int insert3Layer = -1;

        for (int i = 0; i < layers.size(); i++) {
            for (SqlStatement s : layers.get(i)) {
                if (s.getId() == 0) insert1Layer = i;
                if (s.getId() == 1) insert2Layer = i;
                if (s.getId() == 2) commitLayer = i;
                if (s.getId() == 3) insert3Layer = i;
            }
        }

        assertTrue(commitLayer >= 0, "COMMIT should be in a layer");
        assertTrue(insert1Layer <= commitLayer, "INSERT before COMMIT");
        assertTrue(insert2Layer <= commitLayer, "INSERT before COMMIT");
        assertTrue(insert3Layer > commitLayer, "INSERT after COMMIT should be in later layer");
    }

    @Test
    void testConflictKeyGeneration() {
        SqlStatement stmt1 = new SqlStatement.Builder().id(0).seqno(1).sql("test")
                .operationType(SqlStatement.OperationType.INSERT)
                .database("mydb").tableName("users").primaryKeyValue("42").build();
        assertEquals("mydb.users:42", stmt1.getConflictKey());

        SqlStatement stmt2 = new SqlStatement.Builder().id(1).seqno(2).sql("test")
                .operationType(SqlStatement.OperationType.INSERT)
                .database("mydb").tableName("orders").primaryKeyValue("42").build();
        assertEquals("mydb.orders:42", stmt2.getConflictKey());

        assertNotEquals(stmt1.getConflictKey(), stmt2.getConflictKey(),
                "Different tables should have different conflict keys even with same PK value");
    }

    @Test
    void testLargeScaleConcurrency() {
        List<SqlStatement> statements = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            statements.add(new SqlStatement.Builder().id(i).seqno(i + 1)
                    .sql("INSERT INTO db.t1 VALUES (" + i + ")")
                    .operationType(SqlStatement.OperationType.INSERT)
                    .database("db").tableName("t1").primaryKeyValue(String.valueOf(i)).build());
        }

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(1, layers.size(), "1000 different PKs should all be in one layer");
        assertEquals(1000, layers.get(0).size());
    }

    @Test
    void testLargeScaleSequential() {
        List<SqlStatement> statements = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            statements.add(new SqlStatement.Builder().id(i).seqno(i + 1)
                    .sql("UPDATE db.t1 SET x=" + i + " WHERE id=1")
                    .operationType(SqlStatement.OperationType.UPDATE)
                    .database("db").tableName("t1").primaryKeyValue("1").build());
        }

        DependencyGraph graph = new DependencyGraph(statements);
        graph.buildDependencies();
        List<List<SqlStatement>> layers = graph.topologicalSort();

        assertEquals(100, layers.size(), "100 same PK updates should be in 100 layers");
    }
}
