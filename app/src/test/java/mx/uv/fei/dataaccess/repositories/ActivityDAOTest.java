package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(resultId > 0);
    }

    @Test
    void getActivitiesByPractitioner_ExistingPractitioner_ReturnsList() throws DAOException {
        activityDAO.insertActivity(validActivity);

        List<Activity> resultList = activityDAO.getActivitiesByPractitioner(123);

        assertFalse(resultList.isEmpty());
    }
}