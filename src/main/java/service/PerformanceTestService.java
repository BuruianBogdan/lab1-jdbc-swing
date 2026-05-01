package service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import config.AppConfig;
import config.HibernateUtil;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PerformanceTestService {

    public void ruleazaTestConexiuni(Consumer<String> logger) {
        logger.accept("=== test performanta conexiuni ==");

        testeazaFaraPooling(logger);
        testeazaCuPooling(logger);

        logger.accept("===");
    }

    private void testeazaFaraPooling(Consumer<String> logger) {
        String url = AppConfig.get("db.url");
        String username = AppConfig.get("db.username");
        String password = AppConfig.get("db.password");

        long start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                if (!connection.isValid(2)) {
                    throw new RuntimeException("conexiune invalida fara pooling");
                }
            } catch (Exception e) {
                throw new RuntimeException("eroare la testul fara pooling", e);
            }
        }

        long end = System.nanoTime();
        afiseazaRezultate("fara pooling", start, end, logger);
    }

    private void testeazaCuPooling(Consumer<String> logger) {
        long start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.createNativeQuery("select 1").getSingleResult();
            } catch (Exception e) {
                throw new RuntimeException("eroare la testul cu pooling", e);
            }
        }

        long end = System.nanoTime();
        afiseazaRezultate("cu pooling", start, end, logger);
    }

    private void afiseazaRezultate(String tip, long start, long end, Consumer<String> logger) {
        double totalMs = (end - start) / 1_000_000.0;
        double medieMs = totalMs / 100.0;

        logger.accept("test " + tip);
        logger.accept("timp total: " + totalMs + " ms");
        logger.accept("timp mediu / conexiune: " + medieMs + " ms");
        logger.accept("");
    }

    public void demonstreazaConnectionLeak(Consumer<String> logger) {
        logger.accept("== demo leak conexiuni ====");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.get("db.url"));
        config.setUsername(AppConfig.get("db.username"));
        config.setPassword(AppConfig.get("db.password"));
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(2000);

        List<Connection> conexiuniNeinchise = new ArrayList<>();

        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            try {
                logger.accept("deschid conexiunea 1...");
                conexiuniNeinchise.add(dataSource.getConnection());

                logger.accept("deschid conexiunea 2...");
                conexiuniNeinchise.add(dataSource.getConnection());

                logger.accept("deschid conexiunea 3...");
                conexiuniNeinchise.add(dataSource.getConnection());

                logger.accept("pool-ul este acum ocupat complet");
                logger.accept("incerc sa mai obtin inca o conexiune fara sa le inchid pe celelalte...");

                dataSource.getConnection();
                logger.accept("neasteptat: s-a obtinut si a 4-a conexiune");
            } catch (Exception e) {
                logger.accept("pool epuizat, exact cum trebuia demonstrat");
                logger.accept("mesaj: " + e.getMessage());
            } finally {
                logger.accept("");
                logger.accept("inchid corect toate conexiunile...");

                for (Connection connection : conexiuniNeinchise) {
                    try {
                        if (connection != null && !connection.isClosed()) {
                            connection.close();
                        }
                    } catch (Exception ignored) {
                    }
                }

                logger.accept("toate conexiunile au fost inchise corect");
            }
        }

        logger.accept("===");
    }
}