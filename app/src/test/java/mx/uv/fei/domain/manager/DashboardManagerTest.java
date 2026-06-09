package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class DashboardManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private DashboardManager dashboardManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void isAdministratorMenuAvailable_AdminRole_ReturnsTrue() {
        assertTrue(dashboardManager.isAdministratorMenuAvailable("Administrator"));
    }

    @Test
    void isAdministratorMenuAvailable_NotAdmin_ReturnsFalse() {
        assertFalse(dashboardManager.isAdministratorMenuAvailable("Practitioner"));
    }

    @Test
    void isCoordinatorMenuAvailable_CoordinatorRole_ReturnsTrue() {
        assertTrue(dashboardManager.isCoordinatorMenuAvailable("Coordinator"));
    }

    @Test
    void isCoordinatorMenuAvailable_NotCoordinator_ReturnsFalse() {
        assertFalse(dashboardManager.isCoordinatorMenuAvailable("Administrator"));
    }

    @Test
    void isProfessorMenuAvailable_ProfessorRole_ReturnsTrue() {
        assertTrue(dashboardManager.isProfessorMenuAvailable("Professor"));
    }

    @Test
    void isProfessorMenuAvailable_NotProfessor_ReturnsFalse() {
        assertFalse(dashboardManager.isProfessorMenuAvailable("Practitioner"));
    }

    @Test
    void isPractitionerMenuAvailable_PractitionerRole_ReturnsTrue() {
        assertTrue(dashboardManager.isPractitionerMenuAvailable("Practitioner"));
    }

    @Test
    void isPractitionerMenuAvailable_NotPractitioner_ReturnsFalse() {
        assertFalse(dashboardManager.isPractitionerMenuAvailable("Administrator"));
    }

        @Test
    void resolvePractitionerProjectsNavigation_ExistingPractitioner_DoesNotThrow() {
        assertDoesNotThrow(() -> dashboardManager.resolvePractitionerProjectsNavigation(123));
    }

    @Test
    void resolvePractitionerProjectsNavigation_ExistingPractitioner_ReturnsNotNull() throws ManagerException {
        AppSection section = dashboardManager.resolvePractitionerProjectsNavigation(123);

        assertNotNull(section);
    }
}
