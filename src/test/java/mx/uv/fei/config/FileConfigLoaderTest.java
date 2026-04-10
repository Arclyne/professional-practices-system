package mx.uv.fei.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class FileConfigLoaderTest {
    @Test
    void testLoadProperties() {

        Properties baseProperties = FileConfigLoader.loadProperties("database.properties");
        assertNotNull(baseProperties);

    }
}
