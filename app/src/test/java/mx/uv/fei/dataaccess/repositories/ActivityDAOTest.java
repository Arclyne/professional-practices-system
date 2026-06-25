package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ActivityDAOTest {

    private static final int STORED_PRACTITIONER_ID = 123;
    private static final int STORED_REPORT_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IActivityDAO activityDAO;

    private Activity validActivity;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        validActivity = new Activity();
        validActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        validActivity.setTitle("Desarrollo de la capa de acceso a datos");
        validActivity.setDescription("Implementacion de los metodos CRUD del sistema de inventario.");
        validActivity.setStartDate(Date.valueOf("2026-04-15"));
        validActivity.setEndDate(Date.valueOf("2026-04-15"));
        validActivity.setDurationHours(4);
    }

    private Activity buildRequirementsActivity() {
        Activity requirementsActivity = new Activity();
        requirementsActivity.setActivityId(1);
        requirementsActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        requirementsActivity.setReportId(STORED_REPORT_ID);
        requirementsActivity.setTitle("Levantamiento de requisitos");
        requirementsActivity.setDescription("Entrevistas con el personal del area de sistemas");
        requirementsActivity.setStartDate(Date.valueOf("2026-05-01"));
        requirementsActivity.setEndDate(Date.valueOf("2026-05-01"));
        requirementsActivity.setDurationHours(5);
        return requirementsActivity;
    }

    @Test
    void insertActivity_ValidActivity_ReturnsGeneratedId() throws DAOException {
        int resultId = activityDAO.insertActivity(validActivity);

        assertTrue(resultId > 0);
    }

    @Test
    void updateActivity_ValidModifiedData_DoesNotThrow() throws DAOException {
        validActivity.setTitle("Desarrollo de la capa de servicios");
        validActivity.setDescription("Ajustes al alcance de la actividad registrada en la bitacora.");

        assertDoesNotThrow(() -> activityDAO.updateActivity(validActivity, STORED_REPORT_ID));
    }

    @Test
    void getActivitiesByPractitioner_ExistingPractitioner_ReturnsExpectedList() throws DAOException {
        List<Activity> expectedActivities = new ArrayList<>();

        Activity manualActivity = new Activity();
        manualActivity.setActivityId(4);
        manualActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        manualActivity.setReportId(null);
        manualActivity.setTitle("Manual de usuario del sistema");
        manualActivity.setDescription("Avance del manual de usuario para el personal de la empresa");
        manualActivity.setStartDate(Date.valueOf("2026-06-15"));
        manualActivity.setEndDate(Date.valueOf("2026-06-15"));
        manualActivity.setDurationHours(5);
        expectedActivities.add(manualActivity);

        Activity testingActivity = new Activity();
        testingActivity.setActivityId(3);
        testingActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        testingActivity.setReportId(null);
        testingActivity.setTitle("Pruebas de formularios de inventario");
        testingActivity.setDescription("Pruebas funcionales sobre los formularios de captura");
        testingActivity.setStartDate(Date.valueOf("2026-05-03"));
        testingActivity.setEndDate(Date.valueOf("2026-05-03"));
        testingActivity.setDurationHours(3);
        expectedActivities.add(testingActivity);

        Activity developmentActivity = new Activity();
        developmentActivity.setActivityId(2);
        developmentActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        developmentActivity.setReportId(null);
        developmentActivity.setTitle("Desarrollo de pantallas de inventario");
        developmentActivity.setDescription("Maquetado de las pantallas del sistema de inventario");
        developmentActivity.setStartDate(Date.valueOf("2026-05-02"));
        developmentActivity.setEndDate(Date.valueOf("2026-05-02"));
        developmentActivity.setDurationHours(4);
        expectedActivities.add(developmentActivity);

        expectedActivities.add(buildRequirementsActivity());

        List<Activity> resultActivities = activityDAO.getActivitiesByPractitioner(STORED_PRACTITIONER_ID);

        assertEquals(expectedActivities, resultActivities);
    }

    @Test
    void getActivitiesByReport_ExistingReport_ReturnsExpectedList() throws DAOException {
        List<Activity> expectedActivities = new ArrayList<>();
        expectedActivities.add(buildRequirementsActivity());

        List<Activity> resultActivities = activityDAO.getActivitiesByReport(STORED_REPORT_ID);

        assertEquals(expectedActivities, resultActivities);
    }

    @Test
    void assignActivityToReport_ValidIds_DoesNotThrow() throws DAOException {
        assertDoesNotThrow(() -> activityDAO.assignActivityToReport(2, STORED_REPORT_ID));
    }

    @Test
    void removeActivityFromReport_ExistingActivity_DoesNotThrow() throws DAOException {
        assertDoesNotThrow(() -> activityDAO.removeActivityFromReport(1));
    }

    @Test
    void insertActivity_NonExistentPractitioner_ThrowsDAOException() {
        validActivity.setPractitionerId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> activityDAO.insertActivity(validActivity));
    }

    @Test
    void updateActivity_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> activityDAO.updateActivity(validActivity, NON_EXISTENT_ID));
    }

    @Test
    void getActivitiesByPractitioner_NonExistentPractitioner_ReturnsEmptyList() throws DAOException {
        List<Activity> resultActivities = activityDAO.getActivitiesByPractitioner(NON_EXISTENT_ID);

        assertEquals(new ArrayList<>(), resultActivities);
    }

    @Test
    void assignActivityToReport_NonExistentActivity_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> activityDAO.assignActivityToReport(NON_EXISTENT_ID, STORED_REPORT_ID));
    }

    @Test
    void assignActivityToReport_NonExistentReport_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> activityDAO.assignActivityToReport(1, NON_EXISTENT_ID));
    }

    @Test
    void removeActivityFromReport_NonExistentActivity_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> activityDAO.removeActivityFromReport(NON_EXISTENT_ID));
    }
}
