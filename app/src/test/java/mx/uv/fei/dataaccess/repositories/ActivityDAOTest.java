package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IActivityDAO activityDAO;

    private Activity validActivity;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        validActivity = new Activity();
        validActivity.setPractitionerId(123);
        validActivity.setTitle("Desarrollo de DAO");
        validActivity.setDescription("Implementacion de metodos CRUD en Java.");
        validActivity.setActivityDate(java.sql.Date.valueOf("2026-04-15"));
        validActivity.setDurationHours(4);
    }

    @Test
    void insertActivity_ValidActivity_ReturnsGeneratedId() throws DAOException {
        int resultId = activityDAO.insertActivity(validActivity);

        assertTrue(resultId > 0, "El ID generado debería ser mayor a 0 al insertar exitosamente");
    }

    @Test
    void updateActivity_ValidModifiedData_ReturnsTrue() throws DAOException {
        validActivity.setTitle("Titulo Modificado");
        validActivity.setDescription("Descripcion modificada para la prueba de update.");

        boolean isUpdated = activityDAO.updateActivity(validActivity, 1);

        assertTrue(isUpdated, "La actualización debe retornar true al modificar un registro existente");
    }

    @Test
    void getActivitiesByPractitioner_ExistingPractitioner_ReturnsExpectedList() throws DAOException {
        int targetPractitionerId = 123;
        List<Activity> expectedList = new ArrayList<>();

        expectedList.add(buildExpectedActivity(4, 123, null, "Actividad Junio Valida", "Descripcion Junio", "2026-06-15", 5));
        expectedList.add(buildExpectedActivity(3, 123, null, "Dummy 2", "Descripcion Dummy 2", "2026-05-03", 3));
        expectedList.add(buildExpectedActivity(2, 123, null, "Dummy 1", "Descripcion Dummy 1", "2026-05-02", 4));
        expectedList.add(buildExpectedActivity(1, 123, 1, "toRecover", "Descripcion toRecover", "2026-05-01", 5));

        List<Activity> resultList = activityDAO.getActivitiesByPractitioner(targetPractitionerId);

        assertEquals(expectedList, resultList, "La lista de actividades recuperada no coincide con la esperada");
    }

    @Test
    void getActivitiesByReport_ExistingReport_ReturnsExpectedList() throws DAOException {
        int targetReportId = 1;
        List<Activity> expectedList = new ArrayList<>();
        expectedList.add(buildExpectedActivity(1, 123, 1, "toRecover", "Descripcion toRecover", "2026-05-01", 5));

        List<Activity> resultList = activityDAO.getActivitiesByReport(targetReportId);

        assertEquals(expectedList, resultList, "La lista de actividades recuperada no coincide con la esperada");
    }

    @Test
    void assignActivityToReport_ValidIds_ReturnsTrue() throws DAOException {
        boolean isAssigned = activityDAO.assignActivityToReport(2, 1);

        assertTrue(isAssigned, "Debe retornar true al asignar correctamente un reporte a una actividad");
    }

    @Test
    void removeActivityFromReport_ExistingActivity_ReturnsTrue() throws DAOException {
        boolean isRemoved = activityDAO.removeActivityFromReport(1);

        assertTrue(isRemoved, "Debe retornar true al ejecutar la remoción del reporte de la actividad");
    }

    @Test
    void insertActivity_NonExistentPractitioner_ThrowsDAOException() {
        validActivity.setPractitionerId(9999);

        assertThrows(DAOException.class, () -> {
            activityDAO.insertActivity(validActivity);
        }, "Debería lanzar DAOException por violación de Foreign Key al no existir el practitioner_id");
    }

    @Test
    void updateActivity_NonExistentId_ReturnsFalse() throws DAOException {
        int nonExistentActivityId = 9999;

        boolean isUpdated = activityDAO.updateActivity(validActivity, nonExistentActivityId);

        assertFalse(isUpdated, "Debe retornar false al intentar actualizar una actividad que no existe");
    }

    @Test
    void getActivitiesByPractitioner_NonExistentPractitioner_ReturnsEmptyList() throws DAOException {
        List<Activity> resultList = activityDAO.getActivitiesByPractitioner(9999);

        assertTrue(resultList.isEmpty(), "La lista debería estar vacía para un practicante que no existe o no tiene actividades");
    }

    @Test
    void assignActivityToReport_NonExistentActivity_ReturnsFalse() throws DAOException {
        int nonExistentActivityId = 9999;

        boolean isAssigned = activityDAO.assignActivityToReport(nonExistentActivityId, 1);

        assertFalse(isAssigned, "Debe retornar false porque la actividad no existe para ser actualizada");
    }

    @Test
    void assignActivityToReport_NonExistentReport_ThrowsDAOException() {
        int nonExistentReportId = 9999;

        assertThrows(DAOException.class, () -> {
            activityDAO.assignActivityToReport(1, nonExistentReportId);
        }, "Debe lanzar DAOException por violación de Foreign Key al no existir el report_id");
    }

    @Test
    void removeActivityFromReport_NonExistentActivity_ReturnsFalse() throws DAOException {
        int nonExistentActivityId = 9999;

        boolean isRemoved = activityDAO.removeActivityFromReport(nonExistentActivityId);

        assertFalse(isRemoved, "Debe retornar false al intentar remover un reporte de una actividad que no existe");
    }

    private Activity buildExpectedActivity(int id, int practitionerId, Integer reportId, String title, String description, String date, int duration) {
        Activity activity = new Activity();
        activity.setActivityId(id);
        activity.setPractitionerId(practitionerId);
        activity.setReportId(reportId);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setActivityDate(java.sql.Date.valueOf(date));
        activity.setDurationHours(duration);
        return activity;
    }
}