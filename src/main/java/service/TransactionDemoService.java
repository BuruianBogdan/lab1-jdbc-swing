package service;

import dao.EmployeeDAO;
import db.PostgresDatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;

public class TransactionDemoService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public void resetDemoData(Consumer<String> logger) {
        employeeDAO.resetData();
        logger.accept("datele employees au fost resetate");
        logFinalState(logger);
    }

    /* tranzactia A modifica o valoare -> nu face commit ->
    tranzactia B citeste acea valoare necomisa ->apoi A face rollback */
    /*citirea de date necomise*/
    public void runDirtyRead(Consumer<String> logger) {
        logger.accept("=== dirty read ===");
        employeeDAO.resetData();

        Thread transactionA = new Thread(() -> {
            try (Connection connectionA = PostgresDatabaseManager.getConnection()) {
                connectionA.setAutoCommit(false);
                connectionA.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                logger.accept("tranzactia a: begin");
                updateSalary(connectionA, 1, 10000);
                logger.accept("tranzactia a: salariu actualizat la 10000, fara commit");
                Thread.sleep(3000);
                connectionA.rollback();
                logger.accept("tranzactia a: rollback");
            } catch (Exception e) {
                logger.accept("tranzactia a eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            try {
                Thread.sleep(1000);

                try (Connection connectionB = PostgresDatabaseManager.getConnection()) {
                    connectionB.setAutoCommit(false);
                    connectionB.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                    logger.accept("tranzactia b: begin (read uncommitted)");
                    double salary = getSalary(connectionB, 1);
                    logger.accept("tranzactia b: salariu citit = " + salary);
                    connectionB.commit();
                    logger.accept("tranzactia b: commit");
                }
            } catch (Exception e) {
                logger.accept("tranzactia b eroare: " + e.getMessage());
            }
        });

        startAndWait(transactionA, transactionB);
        logFinalState(logger);
    }
    /*tranzactia A citeste o valoare -> trazactia B modifica acea valoare si face commit ->
    * tranzactia A citeste din nou acea valoare si dupa se obtine un alt rezultat*/
    /*aceeasi citire in aceeasi tranzactie dar cu 2 rezultate diferite*/
    public void runNonRepeatableRead(Consumer<String> logger) {
        logger.accept("=== non-repeatable read ===");
        employeeDAO.resetData();

        Thread transactionA = new Thread(() -> {
            try (Connection connectionA = PostgresDatabaseManager.getConnection()) {
                connectionA.setAutoCommit(false);
                connectionA.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                logger.accept("tranzactia a: begin (read committed)");
                double firstSalary = getSalary(connectionA, 1);
                logger.accept("tranzactia a: prima citire = " + firstSalary);

                Thread.sleep(3000);

                double secondSalary = getSalary(connectionA, 1);
                logger.accept("tranzactia a: a doua citire = " + secondSalary);

                connectionA.commit();
                logger.accept("tranzactia a: commit");
            } catch (Exception e) {
                logger.accept("tranzactia a eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            try {
                Thread.sleep(1000);

                try (Connection connectionB = PostgresDatabaseManager.getConnection()) {
                    connectionB.setAutoCommit(false);

                    logger.accept("tranzactia b: begin");
                    updateSalary(connectionB, 1, 12000);
                    connectionB.commit();
                    logger.accept("tranzactia b: actualizat la 12000 si commit");
                }
            } catch (Exception e) {
                logger.accept("tranzactia b eroare: " + e.getMessage());
            }
        });

        startAndWait(transactionA, transactionB);
        logFinalState(logger);
    }
    /*tranzactia A ruleaza  interogare de tip multime, nu doar pe un singur rand
    * tranzactia B insereaza un rand nou care se potriveste pentru conditia inpusa
    * A va rula aceeasi interogare iar numarul de randuri se schimba*/
    public void runPhantomRead(Consumer<String> logger) {
        logger.accept("=== phantom read ===");
        employeeDAO.resetData();

        Thread transactionA = new Thread(() -> {
            try (Connection connectionA = PostgresDatabaseManager.getConnection()) {
                connectionA.setAutoCommit(false);
                connectionA.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

                logger.accept("tranzactia a: begin (repeatable read)");
                int firstCount = countByDepartment(connectionA, 5);
                logger.accept("tranzactia a: prima numaratoare = " + firstCount);

                Thread.sleep(3000);

                int secondCount = countByDepartment(connectionA, 5);
                logger.accept("tranzactia a: a doua numaratoare = " + secondCount);

                connectionA.commit();
                logger.accept("tranzactia a: commit");
            } catch (Exception e) {
                logger.accept("tranzactia a eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            try {
                Thread.sleep(1000);

                try (Connection connectionB = PostgresDatabaseManager.getConnection()) {
                    connectionB.setAutoCommit(false);

                    logger.accept("tranzactia b: begin");
                    insertEmployee(connectionB, "angajat nou", 5, 4700);
                    connectionB.commit();
                    logger.accept("tranzactia b: angajat nou inserat si commit");
                }
            } catch (Exception e) {
                logger.accept("tranzactia b eroare: " + e.getMessage());
            }
        });

        startAndWait(transactionA, transactionB);
        logFinalState(logger);
    }

    /*A si B citesc aceeasi valoare initiala, fiecare calculeaza o noua valoare, fiecare o scrie inapoi
    * iar dupa ultima scriere o suprascrie pe cealalta, una dintre actualizari se pierde*/
    public void runLostUpdate(Consumer<String> logger) {
        logger.accept("=== lost update ===");
        employeeDAO.resetData();

        Thread transactionA = new Thread(() -> {
            try (Connection connectionA = PostgresDatabaseManager.getConnection()) {
                connectionA.setAutoCommit(false);

                logger.accept("tranzactia a: begin");
                double salary = getSalary(connectionA, 1);
                double newSalary = salary + 1000;
                logger.accept("tranzactia a: a citit " + salary + ", va scrie " + newSalary);

                Thread.sleep(3000);

                updateSalary(connectionA, 1, newSalary);
                connectionA.commit();
                logger.accept("tranzactia a: commit");
            } catch (Exception e) {
                logger.accept("tranzactia a eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            try {
                Thread.sleep(1000);

                try (Connection connectionB = PostgresDatabaseManager.getConnection()) {
                    connectionB.setAutoCommit(false);

                    logger.accept("tranzactia b: begin");
                    double salary = getSalary(connectionB, 1);
                    double newSalary = salary + 500;
                    logger.accept("tranzactia b: a citit " + salary + ", va scrie " + newSalary);

                    updateSalary(connectionB, 1, newSalary);
                    connectionB.commit();
                    logger.accept("tranzactia b: commit");
                }
            } catch (Exception e) {
                logger.accept("tranzactia b eroare: " + e.getMessage());
            }
        });

        startAndWait(transactionA, transactionB);
        logFinalState(logger);
    }

    /*A blocheaza un rand, B blocheaza un alt rand, A asteapta randul lui B, iar B asteapta randul lui A,
     fiecare asteapta unul dupa celalalt sistemul detecteaza blocajul circular si anuleaza unda dintre tranzactii*/
    public void runDeadlock(Consumer<String> logger) {
        logger.accept("=== deadlock ===");
        employeeDAO.resetData();

        Thread transactionA = new Thread(() -> {
            try (Connection connectionA = PostgresDatabaseManager.getConnection()) {
                connectionA.setAutoCommit(false);

                logger.accept("tranzactia a: begin");
                updateSalary(connectionA, 1, 6000);
                logger.accept("tranzactia a: a blocat id 1");
                Thread.sleep(2000);
                updateSalary(connectionA, 2, 7000);
                connectionA.commit();
                logger.accept("tranzactia a: commit");
            } catch (Exception e) {
                logger.accept("tranzactia a eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            try {
                Thread.sleep(500);

                try (Connection connectionB = PostgresDatabaseManager.getConnection()) {
                    connectionB.setAutoCommit(false);

                    logger.accept("tranzactia b: begin");
                    updateSalary(connectionB, 2, 6000);
                    logger.accept("tranzactia b: a blocat id 2");
                    Thread.sleep(2000);
                    updateSalary(connectionB, 1, 7000);
                    connectionB.commit();
                    logger.accept("tranzactia b: commit");
                }
            } catch (Exception e) {
                logger.accept("tranzactia b eroare: " + e.getMessage());
            }
        });

        startAndWait(transactionA, transactionB);
        logFinalState(logger);
    }

    public void logFinalState(Consumer<String> logger) {
        logger.accept("starea finala:");
        employeeDAO.getAll().forEach(employee ->
                logger.accept("id=" + employee.getId()
                        + ", name=" + employee.getName()
                        + ", department_id=" + employee.getDepartmentId()
                        + ", salary=" + employee.getSalary())
        );
    }

    private void startAndWait(Thread a, Thread b) {
        a.start();
        b.start();

        try {
            a.join();
            b.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private double getSalary(Connection connection, int id) throws Exception {
        String sql = "select salary from employees where id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        }

        return -1;
    }

    private int countByDepartment(Connection connection, int departmentId) throws Exception {
        String sql = "select count(*) from employees where department_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, departmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        return 0;
    }

    private void updateSalary(Connection connection, int id, double salary) throws Exception {
        String sql = "update employees set salary = ? where id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, salary);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    private void insertEmployee(Connection connection, String name, int departmentId, double salary) throws Exception {
        String sql = "insert into employees(name, department_id, salary) values (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, departmentId);
            statement.setDouble(3, salary);
            statement.executeUpdate();
        }
    }
}