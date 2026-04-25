package db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            InputStream inputStream = DatabaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream("schema.sql");

            if (inputStream == null) {
                throw new RuntimeException("nu s-a gasit fisierul schema.sql");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }

            String[] queries = sqlBuilder.toString().split(";");

            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    statement.execute(query);
                }
            }

            if (isDatabaseEmpty(connection)) {
                insertInitialData(connection);
            }

            System.out.println("baza de date initializata cu succes");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("eroare la initializarea bazei de date", e);
        }
    }

    private static boolean isDatabaseEmpty(Connection connection) {
        String sql = "select count(*) from categorii";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.getInt(1) == 0;
        } catch (Exception e) {
            throw new RuntimeException("eroare la verificarea bazei de date", e);
        }
    }

    private static void insertInitialData(Connection connection) {
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                insert into categorii (nume) values
                ('electronice'),
                ('carti'),
                ('sport')
            """);

            statement.executeUpdate("""
                insert into produse (nume, pret, stoc, categorie_id) values
                ('laptop lenovo', 3500.0, 5, 1),
                ('mouse wireless', 120.0, 20, 1),
                ('java basics', 89.99, 12, 2),
                ('structuri de date', 95.50, 8, 2),
                ('minge fotbal', 75.0, 10, 3),
                ('gantere 5kg', 140.0, 6, 3)
            """);

            statement.executeUpdate("""
                insert into etichete (nume) values
                ('nou'),
                ('reducere'),
                ('popular')
            """);

            statement.executeUpdate("""
                insert into produs_eticheta (produs_id, eticheta_id) values
                (1, 1),
                (1, 3),
                (2, 2),
                (3, 3),
                (5, 1)
            """);

        } catch (Exception e) {
            throw new RuntimeException("eroare la inserarea datelor initiale", e);
        }
    }
}