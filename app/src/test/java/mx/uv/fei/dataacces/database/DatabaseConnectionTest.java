package mx.uv.fei.dataacces.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

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
    void testGetConnectionSuccess() throws SQLException {

        try (Connection connectionTest = dbConnection.getConnection()) {

            assertNotNull(connectionTest, "La conexión devolvió null");
        }
    }
}
