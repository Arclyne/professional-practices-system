package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.etiquette.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class FileConfigLoader {

    private static final String PROFILE_KEY_SEPARATOR = ".";

    public Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (InputStream inputStream = getRequiredResourceStream(fileName)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Critical error reading the file: " + fileName, e);
        }
        return properties;
    }

    public Map<String, String> loadUseConfig(String fileName, String activeProfile) {
        Properties allProperties = loadProperties(fileName);
        Map<String, String> profileProperties = new HashMap<>();
        String profilePrefix = activeProfile + PROFILE_KEY_SEPARATOR;

        for (String key : allProperties.stringPropertyNames()) {
            if (key.startsWith(profilePrefix)) {
                String scopedPropertyKey = key.substring(profilePrefix.length());
                profileProperties.put(scopedPropertyKey, allProperties.getProperty(key));
            }
        }

        return profileProperties;
    }

    private InputStream getRequiredResourceStream(String fileName) {
        InputStream inputStream = FileConfigLoader.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("The configuration file was not found: " + fileName);
        }
        return inputStream;
    }
}