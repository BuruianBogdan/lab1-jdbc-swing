package repository;

import config.HibernateUtil;
import dto.BenchmarkResult;
import dto.PageResult;
import entity.Department;
import entity.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Lab4EmployeeRepository {

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("select count(e) from Employee e", Long.class)
                    .getSingleResult();
        }
    }

    public PageResult<Employee> getEmployeesPageOffset(int pageNumber, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int offset = pageNumber * pageSize;

            List<Employee> employees = session
                    .createQuery("select e from Employee e order by e.id", Employee.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .getResultList();

            long total = session
                    .createQuery("select count(e) from Employee e", Long.class)
                    .getSingleResult();

            Long lastId = employees.isEmpty() ? null : employees.get(employees.size() - 1).getId();

            return new PageResult<>(employees, pageNumber, pageSize, total, lastId);
        }
    }

    public PageResult<Employee> getEmployeesPageCursor(Long lastId, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Employee> employees = session
                    .createQuery(
                            "select e from Employee e where e.id > :lastId order by e.id",
                            Employee.class
                    )
                    .setParameter("lastId", lastId == null ? 0L : lastId)
                    .setMaxResults(pageSize)
                    .getResultList();

            long total = session
                    .createQuery("select count(e) from Employee e", Long.class)
                    .getSingleResult();

            Long newLastId = employees.isEmpty() ? lastId : employees.get(employees.size() - 1).getId();

            return new PageResult<>(employees, 0, pageSize, total, newLastId);
        }
    }

    public void recreateLabData(int departmentsCount, int employeesCount) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createMutationQuery("delete from Employee").executeUpdate();
            session.createMutationQuery("delete from Department").executeUpdate();
            transaction.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<Department> departments = new ArrayList<>();

            for (int i = 1; i <= departmentsCount; i++) {
                Department department = new Department("departament " + i);
                session.persist(department);
                departments.add(department);
            }

            session.flush();

            for (int i = 1; i <= employeesCount; i++) {
                Department department = departments.get((i - 1) % departments.size());

                double salary = 30000 + (i % 90000);

                Employee employee = new Employee(
                        "angajat " + i,
                        "angajat" + i + "@firma.ro",
                        salary,
                        department
                );

                session.persist(employee);

                if (i % 50 == 0) {
                    session.flush();
                    session.clear();

                    departments = session
                            .createQuery("select d from Department d order by d.id", Department.class)
                            .getResultList();
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la generarea datelor pentru laborator", e);
        }
    }

    public void dropLabIndexes() {
        executeSql("drop index if exists idx_employees_email");
        executeSql("drop index if exists idx_employees_department_id");
        executeSql("drop index if exists idx_employees_salary");
        executeSql("drop index if exists idx_employees_dept_salary");
    }

    public void createLabIndexes() {
        executeSql("create index if not exists idx_employees_email on employees(email)");
        executeSql("create index if not exists idx_employees_department_id on employees(department_id)");
        executeSql("create index if not exists idx_employees_salary on employees(salary)");
        executeSql("create index if not exists idx_employees_dept_salary on employees(department_id, salary)");
    }

    public List<BenchmarkResult> runIndexBenchmarks(int repetitions) {
        List<BenchmarkResult> results = new ArrayList<>();

        results.add(runBenchmark(
                "cautare email",
                "select * from employees where email = ?",
                ps -> ps.setString(1, "angajat9999@firma.ro"),
                repetitions
        ));

        results.add(runBenchmark(
                "cautare departament",
                "select * from employees where department_id = ?",
                ps -> ps.setLong(1, 5),
                repetitions
        ));

        results.add(runBenchmark(
                "interval salariu",
                "select * from employees where salary between ? and ?",
                ps -> {
                    ps.setDouble(1, 50000);
                    ps.setDouble(2, 80000);
                },
                repetitions
        ));

        results.add(runBenchmark(
                "multi-coloana",
                "select * from employees where department_id = ? and salary > ?",
                ps -> {
                    ps.setLong(1, 5);
                    ps.setDouble(2, 60000);
                },
                repetitions
        ));

        return results;
    }

    public BenchmarkResult benchmarkOffsetPage(int pageNumber, int pageSize, int repetitions) {
        String sql = "select * from employees order by id limit ? offset ?";

        return runBenchmark(
                "offset page " + pageNumber,
                sql,
                ps -> {
                    ps.setInt(1, pageSize);
                    ps.setInt(2, pageNumber * pageSize);
                },
                repetitions
        );
    }

    public BenchmarkResult benchmarkCursorPage(long lastId, int pageSize, int repetitions) {
        String sql = "select * from employees where id > ? order by id limit ?";

        return runBenchmark(
                "cursor dupa id " + lastId,
                sql,
                ps -> {
                    ps.setLong(1, lastId);
                    ps.setInt(2, pageSize);
                },
                repetitions
        );
    }

    public long updateSalariesIndividually(long departmentId, double percent) {
        long start = System.nanoTime();
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<Employee> employees = session
                    .createQuery(
                            "select e from Employee e where e.department.id = :departmentId",
                            Employee.class
                    )
                    .setParameter("departmentId", departmentId)
                    .getResultList();

            for (Employee employee : employees) {
                employee.setSalary(employee.getSalary() * (1 + percent));
                session.merge(employee);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la update individual", e);
        }

        return elapsedMillis(start);
    }

    public long updateSalariesBulk(long departmentId, double percent) {
        long start = System.nanoTime();
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.createMutationQuery(
                            "update Employee e set e.salary = e.salary * :factor where e.department.id = :departmentId"
                    )
                    .setParameter("factor", 1 + percent)
                    .setParameter("departmentId", departmentId)
                    .executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la update bulk", e);
        }

        return elapsedMillis(start);
    }

    public long updateSalariesBatch(long departmentId, double percent, int batchSize) {
        long start = System.nanoTime();
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<Employee> employees = session
                    .createQuery(
                            "select e from Employee e where e.department.id = :departmentId",
                            Employee.class
                    )
                    .setParameter("departmentId", departmentId)
                    .getResultList();

            for (int i = 0; i < employees.size(); i++) {
                Employee employee = employees.get(i);
                employee.setSalary(employee.getSalary() * (1 + percent));
                session.merge(employee);

                if (i > 0 && i % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la update batch", e);
        }

        return elapsedMillis(start);
    }

    public long preparedStatementWithoutReuse(int repetitions) {
        long start = System.nanoTime();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (int i = 1; i <= repetitions; i++) {
                Long id = (long) ((i % 1000) + 1);

                session.createQuery("select e from Employee e where e.id = :id", Employee.class)
                        .setParameter("id", id)
                        .uniqueResult();
            }
        }

        return elapsedMillis(start);
    }

    public long preparedStatementWithReuse(int repetitions) {
        long start = System.nanoTime();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var query = session.createQuery("select e from Employee e where e.id = :id", Employee.class);

            for (int i = 1; i <= repetitions; i++) {
                Long id = (long) ((i % 1000) + 1);
                query.setParameter("id", id).uniqueResult();
            }
        }

        return elapsedMillis(start);
    }

    private void executeSql(String sql) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            });
        }
    }

    private BenchmarkResult runBenchmark(String name, String sql, SqlBinder binder, int repetitions) {
        long start = System.nanoTime();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(connection -> {
                for (int i = 0; i < repetitions; i++) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                        binder.bind(preparedStatement);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                // consum rezultatul ca masurarea sa includa citirea randurilor
                            }
                        }
                    }
                }
            });
        }

        double averageMillis = elapsedMillis(start) / (double) repetitions;
        String explain = explainAnalyze(sql, binder);

        return new BenchmarkResult(name, averageMillis, explain);
    }

    private String explainAnalyze(String sql, SqlBinder binder) {
        StringBuilder builder = new StringBuilder();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement("explain analyze " + sql)) {
                    binder.bind(preparedStatement);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            builder.append(resultSet.getString(1)).append(System.lineSeparator());
                        }
                    }
                }
            });
        }

        return builder.toString();
    }

    private long elapsedMillis(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement preparedStatement) throws java.sql.SQLException;
    }
}