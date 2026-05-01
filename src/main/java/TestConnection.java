import db.PostgresDatabaseManager;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = PostgresDatabaseManager.getConnection()) {
            System.out.println("Conectare reusita!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}