package mx.uv.fei.dataacces.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class DatabaseConnectionTest {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;

    @BeforeEach
    void setUp() {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
    }

    @Test
    void testGetConnectionSuccess() throws SQLException {

        try (Connection connectionTest = dbConnection.getConnection()) {

            assertNotNull(connectionTest, "La conexión devolvió null");
        }
    }
}
