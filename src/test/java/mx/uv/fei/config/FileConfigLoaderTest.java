package mx.uv.fei.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class FileConfigLoaderTest {
    @Test
    void testLoadPropertiesSuccess() {

        Properties baseProperties = FileConfigLoader.loadProperties("database.properties");
        assertNotNull(baseProperties);

    }
}
