package dao;

import db.PostgresDatabaseManager;
import model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public List<Employee> getAll() {
        List<Employee> employees = new ArrayList<>();
        String sql = "select id, name, department_id, salary from employees order by id";

        try (Connection connection = PostgresDatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                employees.add(new Employee(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("department_id"),
                        resultSet.getDouble("salary")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la citirea angajatilor");
        }

        return employees;
    }

    public double getSalaryById(int id) {
        String sql = "select salary from employees where id = ?";

        try (Connection connection = PostgresDatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la citirea salariului");
        }

        return -1;
    }

    public int countByDepartment(int departmentId) {
        String sql = "select count(*) from employees where department_id = ?";

        try (Connection connection = PostgresDatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, departmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la numararea angajatilor");
        }

        return 0;
    }

    public void resetData() {
        try (Connection connection = PostgresDatabaseManager.getConnection();
             PreparedStatement truncateStmt = connection.prepareStatement("truncate table employees restart identity");
             PreparedStatement insertStmt = connection.prepareStatement("""
                 insert into employees (name, department_id, salary) values
                 ('ana', 5, 5000),
                 ('mihai', 5, 4500),
                 ('ioana', 3, 5500),
                 ('andrei', 2, 4800),
                 ('mara', 5, 5200)
                 """)) {

            truncateStmt.executeUpdate();
            insertStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la resetarea datelor");
        }
    }
}