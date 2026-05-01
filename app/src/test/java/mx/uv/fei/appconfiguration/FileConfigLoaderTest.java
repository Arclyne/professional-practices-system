package mx.uv.fei.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class FileConfigLoaderTest {
    @Test
    void testLoadPropertiesSuccess() {
        FileConfigLoader loader = new FileConfigLoader();

        Properties baseProperties = loader.loadProperties("database.properties");
        assertNotNull(baseProperties);
    }
}
