package mx.uv.fei.dataaccess.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class DatabaseConnectionTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection, "La dependencia IDatabaseConnection no fue inyectada correctamente.");
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void getConnection_ValidConfiguration_ReturnsConnection() throws SQLException {
        try (Connection activeConnection = dbConnection.getConnection()) {
            assertNotNull(activeConnection, "La conexion devolvio null");
        }
    }
}
