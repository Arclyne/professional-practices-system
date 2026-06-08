package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.etiquette.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class FileConfigLoader {

    public Properties loadProperties(String fileName) {
        Properties properties = new Properties();

        try (InputStream input = FileConfigLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IllegalArgumentException("The configuration file was not found: " + fileName);
            }

            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Critical error reading the file: " + fileName, e);
        }

        return properties;
    }

    public Map<String, String> loadUseConfig(String fileName, String useConfig) {
        Properties allProperties = loadProperties(fileName);
        Map<String, String> useConfigMap = new HashMap<>();
        String prefix = useConfig + ".";

        for (String key : allProperties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String propertyName = key.substring(prefix.length());
                useConfigMap.put(propertyName, allProperties.getProperty(key));
            }
        }

        return useConfigMap;
    }
}