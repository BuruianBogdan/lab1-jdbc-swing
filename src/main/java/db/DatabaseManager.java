package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:labjdbc.db";

    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("driverul sqlite nu a fost gasit");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la conectarea la baza de date sqlite");
        }
    }
}