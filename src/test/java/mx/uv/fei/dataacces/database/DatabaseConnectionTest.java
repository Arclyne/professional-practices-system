package mx.uv.fei.dataacces.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class DatabaseConnectionTest {
    @Test
    void testGetConnection() throws SQLException {
        try (Connection connectionTest = DatabaseConnection.getInstance().getConnection()) {

            assertNotNull(connectionTest, "La conexión devolvió null");

            assertFalse(connectionTest.isClosed(), "La conexión se creó pero está cerrada");
        }
    }

    @Test
    void testGetInstance() {
        DatabaseConnection databaseInstance1 = DatabaseConnection.getInstance();
        DatabaseConnection databaseInstance2 = DatabaseConnection.getInstance();

        assertSame(databaseInstance1, databaseInstance2);

    }
}
