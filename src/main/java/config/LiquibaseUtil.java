package config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;

public class LiquibaseUtil {

    private static final String CHANGELOG_FILE = "db/changelog/db.changelog-master.xml";

    public static void updateDatabase() {
        try (Connection connection = DriverManager.getConnection(
                AppConfig.get("db.url"),
                AppConfig.get("db.username"),
                AppConfig.get("db.password")
        )) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try (Liquibase liquibase = new Liquibase(
                    CHANGELOG_FILE,
                    new ClassLoaderResourceAccessor(),
                    database
            )) {
                liquibase.update("");
            }
        } catch (Exception e) {
            throw new RuntimeException("eroare la rularea migrarilor liquibase", e);
        }
    }

    public static void rollbackLastChangeSet() {
        try (Connection connection = DriverManager.getConnection(
                AppConfig.get("db.url"),
                AppConfig.get("db.username"),
                AppConfig.get("db.password")
        )) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try (Liquibase liquibase = new Liquibase(
                    CHANGELOG_FILE,
                    new ClassLoaderResourceAccessor(),
                    database
            )) {
                liquibase.rollback(1, "");
            }
        } catch (Exception e) {
            throw new RuntimeException("eroare la rollback liquibase", e);
        }
    }
}