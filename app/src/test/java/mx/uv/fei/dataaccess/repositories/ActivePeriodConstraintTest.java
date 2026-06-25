package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
class ActivePeriodConstraintTest {

    private static final String SQL_INSERT_PERIOD =
            "INSERT INTO school_period (period_name, start_date, end_date, period_status) VALUES (?, ?, ?, ?)";

    @Inject
    private IDatabaseConnection dbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void insert_SecondActivePeriod_IsRejected() {
        assertThrows(SQLException.class,
                () -> insertPeriod("Periodo Activo Extra", "Active"));
    }

    @Test
    void insert_UpcomingPeriod_IsAllowed() {
        assertDoesNotThrow(
                () -> insertPeriod("Periodo Proximo", "Upcoming"));
    }

    @Test
    void insert_ActivePeriod_AfterConcludingCurrent_IsAllowed() {
        assertDoesNotThrow(() -> {
            concludeAllPeriods();
            insertPeriod("Nuevo Periodo Activo", "Active");
        });
    }

    private void insertPeriod(String name, String status) throws SQLException {
        try (Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PERIOD)) {
            statement.setString(1, name);
            statement.setDate(2, java.sql.Date.valueOf("2030-08-01"));
            statement.setDate(3, java.sql.Date.valueOf("2031-01-31"));
            statement.setString(4, status);
            statement.executeUpdate();
        }
    }

    private void concludeAllPeriods() throws SQLException {
        try (Connection connection = dbConnection.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE school_period SET period_status = 'Concluded' WHERE period_status = 'Active'");
        }
    }
}
