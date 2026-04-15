package mx.uv.fei.dataacces.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import mx.uv.fei.TestApp;
import mx.uv.fei.TestConfig;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class DatabaseConnectionTest {
    @Autowired
    private IDatabaseConnection dbConnection;

    @Test
    void testGetConnection() throws SQLException {

        try (Connection connectionTest = dbConnection.getConnection()) {

            assertNotNull(connectionTest, "La conexión devolvió null");

            assertFalse(connectionTest.isClosed(), "La conexión se creó pero está cerrada");
        }
    }
}
