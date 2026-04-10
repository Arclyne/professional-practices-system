package mx.uv.fei.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import mx.uv.fei.dataacces.exceptions.ConfigExeption;

public class FileConfigLoader {
    public static Properties loadProperties(String fileName) {
        Properties properties = new Properties();

        try (
                InputStream input = FileConfigLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IllegalArgumentException("The configuration file was not found: " + fileName);
            }

            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Critical error reading the file: " + fileName, ex);
        }

        return properties;
    }

    public static Map<String, String> loadUseConfig(String fileName, String useConfig) throws ConfigExeption {
        Properties Allproperties = loadProperties(fileName);
        Map<String, String> useConfigMap = new HashMap<>();
        String prefix = useConfig + ".";

        for (String key : Allproperties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String propety = key.substring(prefix.length());
                useConfigMap.put(propety, Allproperties.getProperty(key));
            }
        }
        if (useConfigMap.isEmpty()) {
            throw new ConfigExeption("No existe la configuracion");
        }
        return useConfigMap;
    }
}