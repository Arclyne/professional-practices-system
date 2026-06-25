package mx.uv.fei.domain.manager.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ActivityManagerTest {

    private static final int STORED_PRACTITIONER_ID = 123;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ActivityManager activityManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerActivity_ValidActivity_DoesNotThrow() {
        Activity newActivity = new Activity();
        newActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        newActivity.setTitle("Capacitacion en herramientas internas");
        newActivity.setDescription("Induccion al uso de los sistemas internos de la empresa.");
        newActivity.setActivityDate(Date.valueOf("2026-06-01"));
        newActivity.setDurationHours(2);

        assertDoesNotThrow(() -> activityManager.registerActivity(newActivity));
    }

    @Test
    void getPractitionerLogbook_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Activity> expectedActivities = new ArrayList<>();

        Activity manualActivity = new Activity();
        manualActivity.setActivityId(4);
        manualActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        manualActivity.setReportId(null);
        manualActivity.setTitle("Manual de usuario del sistema");
        manualActivity.setDescription("Avance del manual de usuario para el personal de la empresa");
        manualActivity.setActivityDate(Date.valueOf("2026-06-15"));
        manualActivity.setDurationHours(5);
        expectedActivities.add(manualActivity);

        Activity testingActivity = new Activity();
        testingActivity.setActivityId(3);
        testingActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        testingActivity.setReportId(null);
        testingActivity.setTitle("Pruebas de formularios de inventario");
        testingActivity.setDescription("Pruebas funcionales sobre los formularios de captura");
        testingActivity.setActivityDate(Date.valueOf("2026-05-03"));
        testingActivity.setDurationHours(3);
        expectedActivities.add(testingActivity);

        Activity developmentActivity = new Activity();
        developmentActivity.setActivityId(2);
        developmentActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        developmentActivity.setReportId(null);
        developmentActivity.setTitle("Desarrollo de pantallas de inventario");
        developmentActivity.setDescription("Maquetado de las pantallas del sistema de inventario");
        developmentActivity.setActivityDate(Date.valueOf("2026-05-02"));
        developmentActivity.setDurationHours(4);
        expectedActivities.add(developmentActivity);

        Activity requirementsActivity = new Activity();
        requirementsActivity.setActivityId(1);
        requirementsActivity.setPractitionerId(STORED_PRACTITIONER_ID);
        requirementsActivity.setReportId(1);
        requirementsActivity.setTitle("Levantamiento de requisitos");
        requirementsActivity.setDescription("Entrevistas con el personal del area de sistemas");
        requirementsActivity.setActivityDate(Date.valueOf("2026-05-01"));
        requirementsActivity.setDurationHours(5);
        expectedActivities.add(requirementsActivity);

        List<Activity> resultActivities = activityManager.getPractitionerLogbook(STORED_PRACTITIONER_ID);

        assertEquals(expectedActivities, resultActivities);
    }
}
