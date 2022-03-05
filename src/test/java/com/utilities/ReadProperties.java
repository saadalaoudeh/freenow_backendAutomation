package com.utilities;


import enums.Environment;
import exceptions.FileNotFound;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class ReadProperties {
    private final Properties prop = new Properties();

    public ReadProperties() {
        Environment environment;
        String s_environment;
        if (System.getProperty("environment") == null || System.getProperty("environment").equals("")) {
            s_environment = Environment.TEST.name();
        } else
            s_environment = System.getProperty("environment").replace("i", "ı").toUpperCase();

        try {
            environment = Environment.valueOf(s_environment);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("UndefindedEnum \"" + s_environment + "\"");
        }

        String configPath = readEnvironment(environment);
        try {
            loadConfigProperties(configPath);
        } catch (Exception e) {
        }
    }

    private void loadConfigProperties(String path) throws FileNotFound {
        try {
            InputStream in = new FileInputStream(path);
            prop.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new FileNotFound("Properties file not found path: " + path);
        }
    }

    public String readEnvironment(Environment environment) {

        switch (environment) {
            case PROD:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("prop.environment/freenow-prod-environment.properties")).getFile();
            case TEST:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("prop.environment/freenow-test-environment.properties")).getFile();
            case STAGING:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("prop.environment/freenow-staging-environment.properties")).getFile();
            case HOTFIX:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("prop.environment/freenow-hotfix-environment.properties")).getFile();
            default:
                throw new RuntimeException("Undefined environment ");
        }
    }

    public String get(String key) {
        return prop.getProperty(key);
    }


}