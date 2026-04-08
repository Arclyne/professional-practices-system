package mx.uv.fei.config;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class FileConfigLoader {
    public static Properties loadProperties(String fileName) {
        Properties properties = new Properties();

        try (
            InputStream input = FileConfigLoader.class.getClassLoader().getResourceAsStream(fileName)
        ) {
            if (input == null) {
                throw new IllegalArgumentException("The configuration file was not found: " + fileName);
            }
            
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Critical error reading the file: " + fileName, ex);
        }

        return properties;
    }
}