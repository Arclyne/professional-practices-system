package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

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
        assertNotNull(dbConnection, "La conexión a BD debe ser inyectada.");
        assertNotNull(activityDAO, "El DAO debe ser inyectado.");
        TestDatabaseSetup.initialize(dbConnection);

        validActivity = new Activity();
        validActivity.setPractitionerId(1);
        validActivity.setTitle("Desarrollo de DAO");
        validActivity.setDescription("Implementación de métodos CRUD en Java.");
        validActivity.setActivityDate(java.sql.Date.valueOf("2026-04-15"));
        validActivity.setDurationHours(4);
    }

    @Test
    void insertActivity_ValidActivity_ReturnsGeneratedId() throws DAOException {
        int resultId = activityDAO.insertActivity(validActivity);

        assertTrue(resultId > 0, "La actividad debió insertarse y generar un ID mayor a cero.");
    }

    @Test
    void getActivitiesByPractitioner_ExistingPractitioner_ReturnsList() throws DAOException {

        activityDAO.insertActivity(validActivity);
        int targetPractitionerId = 1;


        List<Activity> activities = activityDAO.getActivitiesByPractitioner(targetPractitionerId);


        assertNotNull(activities, "La lista de actividades no debe ser nula.");
        assertTrue(activities.size() > 0, "Debe contener al menos la actividad recién insertada.");
    }
}