package mx.uv.fei.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import org.junit.jupiter.api.Test;

public class FileConfigLoaderTest {

    @Test
    void loadProperties_ValidFileName_ReturnsProperties() {
        FileConfigLoader loader = new FileConfigLoader();

        Properties baseProperties = loader.loadProperties("database.properties");
        assertNotNull(baseProperties);
    }

    @Test
    void loadProperties_NonExistentFile_ThrowsException() {
        FileConfigLoader loader = new FileConfigLoader();

        assertThrows(RuntimeException.class, () -> {
            loader.loadProperties("archivo_que_no_existe.properties");
        }, "Debería lanzar una excepción al intentar cargar un archivo inexistente");
    }

    @Test
    void loadProperties_NullFileName_ThrowsIllegalArgumentException() {
        FileConfigLoader loader = new FileConfigLoader();

        assertThrows(RuntimeException.class, () -> {
            loader.loadProperties(null);
        }, "Debería lanzar IllegalArgumentException si el nombre del archivo es null");
    }
}
