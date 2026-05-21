package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import entity.Categorie;
import entity.Department;
import entity.Employee;
import entity.Produs;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.Properties;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                HikariConfig hikariConfig = new HikariConfig();

                hikariConfig.setJdbcUrl(AppConfig.get("db.url"));
                hikariConfig.setUsername(AppConfig.get("db.username"));
                hikariConfig.setPassword(AppConfig.get("db.password"));

                hikariConfig.setMaximumPoolSize(Integer.parseInt(AppConfig.get("hikari.maximumPoolSize")));
                hikariConfig.setMinimumIdle(Integer.parseInt(AppConfig.get("hikari.minimumIdle")));
                hikariConfig.setConnectionTimeout(Long.parseLong(AppConfig.get("hikari.connectionTimeout")));
                hikariConfig.setIdleTimeout(Long.parseLong(AppConfig.get("hikari.idleTimeout")));
                hikariConfig.setMaxLifetime(Long.parseLong(AppConfig.get("hikari.maxLifetime")));
                hikariConfig.setConnectionTestQuery(AppConfig.get("hikari.connectionTestQuery"));

                HikariDataSource dataSource = new HikariDataSource(hikariConfig);

                Properties settings = new Properties();
                settings.put(Environment.DATASOURCE, dataSource);
                settings.put(Environment.HBM2DDL_AUTO, AppConfig.get("hibernate.hbm2ddl.auto"));
                settings.put(Environment.SHOW_SQL, AppConfig.get("hibernate.show_sql"));
                settings.put(Environment.FORMAT_SQL, AppConfig.get("hibernate.format_sql"));
                settings.put(Environment.GENERATE_STATISTICS, AppConfig.get("hibernate.generate_statistics"));
                settings.put(Environment.STATEMENT_BATCH_SIZE, AppConfig.get("hibernate.jdbc.batch_size"));
                settings.put(Environment.ORDER_INSERTS, "true");
                settings.put(Environment.ORDER_UPDATES, "true");
                settings.put("hibernate.use_sql_comments", "true");
                settings.put("hibernate.highlight_sql", AppConfig.get("hibernate.highlight_sql"));

                Configuration configuration = new Configuration();
                configuration.setProperties(settings);

                // entitatile vechi din proiectul tau
                configuration.addAnnotatedClass(Categorie.class);
                configuration.addAnnotatedClass(Produs.class);

                // entitatile noi pentru lab 4
                configuration.addAnnotatedClass(Department.class);
                configuration.addAnnotatedClass(Employee.class);

                sessionFactory = configuration.buildSessionFactory();
            } catch (Exception e) {
                throw new RuntimeException("eroare la initializarea hibernate", e);
            }
        }

        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}