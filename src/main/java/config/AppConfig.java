package config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("fisierul application.properties nu a fost gasit");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("eroare la incarcarea configurarii", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}