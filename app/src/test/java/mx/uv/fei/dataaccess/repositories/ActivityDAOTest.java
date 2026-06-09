package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
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
    void getActivitiesByPractitioner_ExistingPractitioner_ReturnsList() throws DAOException {
        List<Activity> resultList = activityDAO.getActivitiesByPractitioner(123);

        assertFalse(resultList.isEmpty(), "La lista no debería estar vacía para un practicante con actividades previas");
    }

    @Test
    void getActivitiesByReport_ExistingReport_ReturnsList() throws DAOException {
        List<Activity> resultList = activityDAO.getActivitiesByReport(1);

        assertNotNull(resultList, "El método debe retornar una lista inicializada (vacía o con elementos), nunca null");
    }

    @Test
    void assignActivityToReport_ValidIds_ReturnsTrue() throws DAOException {
        boolean isAssigned = activityDAO.assignActivityToReport(1, 1);

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
}
