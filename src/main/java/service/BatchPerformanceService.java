package service;

import db.PostgresDatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BatchPerformanceService {

    private static final int INSERT_COUNT = 5000;
    private static final int RUNS = 3;

    public void runBatchComparison(Consumer<String> logger) {
        logger.accept("=== comparatie batch insert ===");

        List<Long> autoCommitRuns = new ArrayList<>();
        List<Long> commit100Runs = new ArrayList<>();
        List<Long> singleBatchRuns = new ArrayList<>();

        for (int i = 1; i <= RUNS; i++) {
            logger.accept("rulare " + i);

            cleanupBatchRows();
            long t1 = runAutoCommit();
            autoCommitRuns.add(t1);
            logger.accept("auto-commit: " + t1 + " ms");

            cleanupBatchRows();
            long t2 = runCommitEvery100();
            commit100Runs.add(t2);
            logger.accept("commit la 100: " + t2 + " ms");

            cleanupBatchRows();
            long t3 = runSingleTransactionBatch();
            singleBatchRuns.add(t3);
            logger.accept("single transaction + batch: " + t3 + " ms");
        }

        logger.accept("");
        logger.accept("rezultate finale:");
        logger.accept("abordare | run1 | run2 | run3 | medie");
        logger.accept(buildRow("auto-commit", autoCommitRuns));
        logger.accept(buildRow("commit-100", commit100Runs));
        logger.accept(buildRow("single-batch", singleBatchRuns));
    }

    private long runAutoCommit() {
        long start = System.currentTimeMillis();

        try (Connection connection = PostgresDatabaseManager.getConnection()) {
            String sql = "insert into employees(name, department_id, salary) values (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < INSERT_COUNT; i++) {
                    statement.setString(1, "emp_auto_" + i);
                    statement.setInt(2, 99);
                    statement.setDouble(3, 3000 + i);
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("eroare la auto-commit", e);
        }

        return System.currentTimeMillis() - start;
    }

    private long runCommitEvery100() {
        long start = System.currentTimeMillis();

        try (Connection connection = PostgresDatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            String sql = "insert into employees(name, department_id, salary) values (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < INSERT_COUNT; i++) {
                    statement.setString(1, "emp_100_" + i);
                    statement.setInt(2, 99);
                    statement.setDouble(3, 3000 + i);
                    statement.executeUpdate();

                    if ((i + 1) % 100 == 0) {
                        connection.commit();
                    }
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("eroare la commit la 100", e);
        }

        return System.currentTimeMillis() - start;
    }

    private long runSingleTransactionBatch() {
        long start = System.currentTimeMillis();

        try (Connection connection = PostgresDatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            String sql = "insert into employees(name, department_id, salary) values (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < INSERT_COUNT; i++) {
                    statement.setString(1, "emp_batch_" + i);
                    statement.setInt(2, 99);
                    statement.setDouble(3, 3000 + i);
                    statement.addBatch();

                    if ((i + 1) % 50 == 0) {
                        statement.executeBatch();
                    }
                }

                statement.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("eroare la single transaction batch", e);
        }

        return System.currentTimeMillis() - start;
    }

    private void cleanupBatchRows() {
        try (Connection connection = PostgresDatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from employees where department_id = 99");
        } catch (Exception e) {
            throw new RuntimeException("eroare la curatarea randurilor batch", e);
        }
    }

    private String buildRow(String label, List<Long> values) {
        long average = Math.round(values.stream().mapToLong(Long::longValue).average().orElse(0));
        return label + " | " + values.get(0) + " | " + values.get(1) + " | " + values.get(2) + " | " + average;
    }
}