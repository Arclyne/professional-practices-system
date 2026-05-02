package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

@StartEtiquetteTest
@Profile("test")
public class ActivityDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IActivityDAO activityDAOTest;

    private Activity expectedActivityInserted;
    private List<Activity> expectedList;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection, "dbConnection no fue inyectado.");
        assertNotNull(activityDAOTest, "activityDAOTest no fue inyectado.");

        TestDatabaseSetup.initialize(dbConnection);

        expectedActivityInserted = new Activity();
        expectedActivityInserted.setActivityId(1);
        expectedActivityInserted.setName("toRecover");
        expectedActivityInserted.setManager("toRecover");

        expectedList = new ArrayList<>();
        expectedList.add(expectedActivityInserted);
    }

    @Test
    void testInsertActivitySuccess() throws DAOException {
        Activity testActivity = new Activity();
        testActivity.setName("Diseño de la Base de Datos");
        testActivity.setManager("Josep Prueba");

        testActivity.setStartDate(java.sql.Date.valueOf("2026-04-15"));
        testActivity.setEndDate(java.sql.Date.valueOf("2026-04-30"));

        boolean result = activityDAOTest.insertActivity(testActivity);
        assertTrue(result, "La actividad debería haberse insertado correctamente.");
    }

    @Test
    void testRecoverActivitySuccess() throws DAOException {

        Activity resultTest = activityDAOTest.recoverActivity("toRecover", "toRecover");

        assertNotNull(resultTest, "No se pudo recuperar la actividad esperada.");
        assertEquals(expectedActivityInserted, resultTest, "La actividad recuperada no coincide con la esperada.");
    }
}