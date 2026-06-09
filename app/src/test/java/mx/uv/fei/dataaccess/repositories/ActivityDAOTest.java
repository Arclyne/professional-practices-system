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

        assertTrue(resultId > 0);
    }

    @Test
    void updateActivity_ValidModifiedData_ReturnsTrue() throws DAOException {
        validActivity.setTitle("Titulo Modificado");
        validActivity.setDescription("Descripcion modificada para la prueba de update.");

        boolean isUpdated = activityDAO.updateActivity(validActivity, 1);

        assertTrue(isUpdated);
    }

    @Test
    void getActivitiesByPractitioner_ExistingPractitioner_ReturnsExpectedList() throws DAOException {
        int targetPractitionerId = 123;
        List<Activity> expectedList = new ArrayList<>();

        Activity act4 = new Activity();
        act4.setActivityId(4);
        act4.setPractitionerId(123);
        act4.setReportId(null);
        act4.setTitle("Actividad Junio Valida");
        act4.setDescription("Descripcion Junio");
        act4.setActivityDate(java.sql.Date.valueOf("2026-06-15"));
        act4.setDurationHours(5);
        expectedList.add(act4);

        Activity act3 = new Activity();
        act3.setActivityId(3);
        act3.setPractitionerId(123);
        act3.setReportId(null);
        act3.setTitle("Dummy 2");
        act3.setDescription("Descripcion Dummy 2");
        act3.setActivityDate(java.sql.Date.valueOf("2026-05-03"));
        act3.setDurationHours(3);
        expectedList.add(act3);

        Activity act2 = new Activity();
        act2.setActivityId(2);
        act2.setPractitionerId(123);
        act2.setReportId(null);
        act2.setTitle("Dummy 1");
        act2.setDescription("Descripcion Dummy 1");
        act2.setActivityDate(java.sql.Date.valueOf("2026-05-02"));
        act2.setDurationHours(4);
        expectedList.add(act2);

        Activity act1 = new Activity();
        act1.setActivityId(1);
        act1.setPractitionerId(123);
        act1.setReportId(1);
        act1.setTitle("toRecover");
        act1.setDescription("Descripcion toRecover");
        act1.setActivityDate(java.sql.Date.valueOf("2026-05-01"));
        act1.setDurationHours(5);
        expectedList.add(act1);

        List<Activity> resultList = activityDAO.getActivitiesByPractitioner(targetPractitionerId);

        assertEquals(expectedList, resultList);
    }

    @Test
    void getActivitiesByReport_ExistingReport_ReturnsExpectedList() throws DAOException {
        int targetReportId = 1;
        List<Activity> expectedList = new ArrayList<>();

        Activity act1 = new Activity();
        act1.setActivityId(1);
        act1.setPractitionerId(123);
        act1.setReportId(1);
        act1.setTitle("toRecover");
        act1.setDescription("Descripcion toRecover");
        act1.setActivityDate(java.sql.Date.valueOf("2026-05-01"));
        act1.setDurationHours(5);
        expectedList.add(act1);

        List<Activity> resultList = activityDAO.getActivitiesByReport(targetReportId);

        assertEquals(expectedList, resultList);
    }

    @Test
    void assignActivityToReport_ValidIds_ReturnsTrue() throws DAOException {
        boolean isAssigned = activityDAO.assignActivityToReport(2, 1);

        assertTrue(isAssigned);
    }

    @Test
    void removeActivityFromReport_ExistingActivity_ReturnsTrue() throws DAOException {
        boolean isRemoved = activityDAO.removeActivityFromReport(1);

        assertTrue(isRemoved);
    }

    @Test
    void insertActivity_NonExistentPractitioner_ThrowsDAOException() {
        validActivity.setPractitionerId(9999);

        assertThrows(DAOException.class, () -> {
            activityDAO.insertActivity(validActivity);
        });
    }

    @Test
    void updateActivity_NonExistentId_ReturnsFalse() throws DAOException {
        int nonExistentActivityId = 9999;

        boolean isUpdated = activityDAO.updateActivity(validActivity, nonExistentActivityId);

        assertFalse(isUpdated);
    }

    @Test
    void getActivitiesByPractitioner_NonExistentPractitioner_ReturnsEmptyList() throws DAOException {
        List<Activity> resultList = activityDAO.getActivitiesByPractitioner(9999);

        assertEquals(new ArrayList<>(), resultList);
    }

    @Test
    void assignActivityToReport_NonExistentActivity_ReturnsFalse() throws DAOException {
        int nonExistentActivityId = 9999;

        boolean isAssigned = activityDAO.assignActivityToReport(nonExistentActivityId, 1);

        assertFalse(isAssigned);
    }

    @Test
    void assignActivityToReport_NonExistentReport_ThrowsDAOException() {
        int nonExistentReportId = 9999;

        assertThrows(DAOException.class, () -> {
            activityDAO.assignActivityToReport(1, nonExistentReportId);
        });
    }

    @Test
    void removeActivityFromReport_NonExistentActivity_ReturnsFalse() throws DAOException {
        int nonExistentActivityId = 9999;

        boolean isRemoved = activityDAO.removeActivityFromReport(nonExistentActivityId);

        assertFalse(isRemoved);
    }
}