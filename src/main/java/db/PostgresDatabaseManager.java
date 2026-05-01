package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresDatabaseManager {

    private static final String URL = "jdbc:postgresql://localhost:5431/lab2_transactions_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres"; // pune parola ta reala

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}