package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class GroupEnrollmentManagerTest {

    private static final int STORED_PRACTITIONER_ID = 123;
    private static final int STORED_GROUP_ID = 6;
    private static final int SECOND_PERIOD_ID = 7;
    private static final int SECOND_GROUP_ID = 8;
    private static final int STORED_PROFESSOR_ID = 68;
    private static final int NON_EXISTENT_GROUP_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private GroupEnrollmentManager groupEnrollmentManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void enrollPractitionerInGroup_DifferentPeriod_ReturnsGeneratedId() throws ManagerException, SQLException {
        createSecondPeriodAndGroup();

        int generatedId = groupEnrollmentManager.enrollPractitionerInGroup(STORED_PRACTITIONER_ID, SECOND_GROUP_ID);

        assertTrue(generatedId > 0);
    }

    @Test
    void enrollPractitionerInGroup_SamePeriodAlreadyEnrolled_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> groupEnrollmentManager.enrollPractitionerInGroup(STORED_PRACTITIONER_ID, STORED_GROUP_ID));
    }

    @Test
    void enrollPractitionerInGroup_NonExistentGroup_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> groupEnrollmentManager.enrollPractitionerInGroup(STORED_PRACTITIONER_ID, NON_EXISTENT_GROUP_ID));
    }

    private void createSecondPeriodAndGroup() throws SQLException {
        String insertPeriod = "INSERT INTO school_period (period_id, period_name, period_status) "
                + "VALUES (" + SECOND_PERIOD_ID + ", 'Enero-Junio 2027', 'Upcoming')";
        String insertGroup = "INSERT INTO practice_group (group_id, section, period_id, professor_id) "
                + "VALUES (" + SECOND_GROUP_ID + ", '60124', " + SECOND_PERIOD_ID + ", " + STORED_PROFESSOR_ID + ")";

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(insertPeriod);
            statement.execute(insertGroup);
        }
    }
}
