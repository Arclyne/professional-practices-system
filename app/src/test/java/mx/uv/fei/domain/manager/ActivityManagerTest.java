package mx.uv.fei.domain.manager;

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
        Activity activity = new Activity();
        activity.setPractitionerId(123);
        activity.setTitle("Test");
        activity.setDescription("Desc");
        activity.setActivityDate(Date.valueOf("2026-06-01"));
        activity.setDurationHours(2);

        assertDoesNotThrow(() -> activityManager.registerActivity(activity));
    }

    @Test
    void getPractitionerLogbook_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Activity> expectedList = new ArrayList<>();

        Activity act4 = new Activity();
        act4.setActivityId(4);
        act4.setPractitionerId(123);
        act4.setReportId(null);
        act4.setTitle("Actividad Junio Valida");
        act4.setDescription("Descripcion Junio");
        act4.setActivityDate(Date.valueOf("2026-06-15"));
        act4.setDurationHours(5);
        expectedList.add(act4);

        Activity act3 = new Activity();
        act3.setActivityId(3);
        act3.setPractitionerId(123);
        act3.setReportId(null);
        act3.setTitle("Dummy 2");
        act3.setDescription("Descripcion Dummy 2");
        act3.setActivityDate(Date.valueOf("2026-05-03"));
        act3.setDurationHours(3);
        expectedList.add(act3);

        Activity act2 = new Activity();
        act2.setActivityId(2);
        act2.setPractitionerId(123);
        act2.setReportId(null);
        act2.setTitle("Dummy 1");
        act2.setDescription("Descripcion Dummy 1");
        act2.setActivityDate(Date.valueOf("2026-05-02"));
        act2.setDurationHours(4);
        expectedList.add(act2);

        Activity act1 = new Activity();
        act1.setActivityId(1);
        act1.setPractitionerId(123);
        act1.setReportId(1);
        act1.setTitle("toRecover");
        act1.setDescription("Descripcion toRecover");
        act1.setActivityDate(Date.valueOf("2026-05-01"));
        act1.setDurationHours(5);
        expectedList.add(act1);

        List<Activity> resultList = activityManager.getPractitionerLogbook(123);
        assertEquals(expectedList, resultList);
    }
}