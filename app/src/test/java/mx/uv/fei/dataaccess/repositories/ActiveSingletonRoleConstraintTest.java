package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifica que la restricción de unicidad parcial (un solo usuario ACTIVO por rol único) se cumple a
 * nivel de base de datos —no solo en la aplicación—, cerrando la condición de carrera SELECT/INSERT.
 * data.sql siembra un administrador y un coordinador activos.
 */
@StartEtiquetteTest
@Profile("test")
class ActiveSingletonRoleConstraintTest {

    private static final String SQL_INSERT_USER =
            "INSERT INTO user (username, password, name, last_name, email, role_name, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    @Inject
    private IDatabaseConnection dbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void insert_SecondActiveAdministrator_IsRejected() {
        assertThrows(SQLException.class,
                () -> insertUser("90011111", "admin2@uv.mx", "Administrator", "Active"));
    }

    @Test
    void insert_SecondActiveCoordinator_IsRejected() {
        assertThrows(SQLException.class,
                () -> insertUser("90022222", "coord2@uv.mx", "Coordinator", "Active"));
    }

    @Test
    void insert_InactiveCoordinator_IsAllowed() {
        assertDoesNotThrow(
                () -> insertUser("90033333", "coord3@uv.mx", "Coordinator", "Inactive"));
    }

    @Test
    void insert_ActiveProfessor_IsAllowed_RoleNotConstrained() {
        assertDoesNotThrow(
                () -> insertUser("90044444", "profe2@uv.mx", "Professor", "Active"));
    }

    private void insertUser(String username, String email, String role, String status) throws SQLException {
        try (Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT_USER)) {
            statement.setString(1, username);
            statement.setString(2, "ClaveUv2026");
            statement.setString(3, "Nombre");
            statement.setString(4, "Apellido");
            statement.setString(5, email);
            statement.setString(6, role);
            statement.setString(7, status);
            statement.executeUpdate();
        }
    }
}
