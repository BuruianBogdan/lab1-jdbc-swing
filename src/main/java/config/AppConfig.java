package config;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppConfig {

    private static final Properties properties = new Properties();
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?}");

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
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("proprietatea lipseste: " + key);
        }
        return resolveEnvironmentVariables(value);
    }

    private static String resolveEnvironmentVariables(String value) {
        Matcher matcher = ENV_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String envName = matcher.group(1);
            String defaultValue = matcher.group(2) == null ? "" : matcher.group(2);
            String envValue = System.getenv().getOrDefault(envName, defaultValue);
            matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}