package mx.uv.fei.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileConfigLoaderTest {

    private FileConfigLoader configLoader;

    @BeforeEach
    void setUp() {
        configLoader = new FileConfigLoader();
    }

    @Test
    void loadProperties_ValidFileName_ReturnsProperties() {
        Properties databaseProperties = configLoader.loadProperties("database.properties");

        assertNotNull(databaseProperties);
    }

    @Test
    void loadProperties_NonExistentFile_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> configLoader.loadProperties("configuracion_inexistente.properties"),
                "Deberia lanzar una excepcion al intentar cargar un archivo inexistente");
    }

    @Test
    void loadProperties_NullFileName_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> configLoader.loadProperties(null),
                "Deberia lanzar una excepcion si el nombre del archivo es null");
    }
}
