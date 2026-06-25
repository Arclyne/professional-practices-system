package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.enums.EnrollmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class GroupEnrollmentDAOTest {

    private static final int STORED_PRACTITIONER_ID = 123;
    private static final int STORED_GROUP_ID = 6;
    private static final int STORED_PERIOD_ID = 5;
    private static final int NON_EXISTENT_PERIOD_ID = 9999;
    private static final int SECOND_PERIOD_ID = 7;
    private static final int SECOND_GROUP_ID = 8;
    private static final int STORED_PROFESSOR_ID = 68;
    private static final int FIRST_OPPORTUNITY = 1;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IGroupEnrollmentDAO enrollmentDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void recoverEnrollmentByPractitionerAndPeriod_ExistingEnrollment_ReturnsEnrollment() throws DAOException {
        GroupEnrollment recoveredEnrollment = enrollmentDAO
                .recoverEnrollmentByPractitionerAndPeriod(STORED_PRACTITIONER_ID, STORED_PERIOD_ID);

        assertNotNull(recoveredEnrollment);
        assertEquals(STORED_GROUP_ID, recoveredEnrollment.getGroupId());
    }

    @Test
    void recoverEnrollmentByPractitionerAndPeriod_NonExistentPeriod_ReturnsNull() throws DAOException {
        GroupEnrollment recoveredEnrollment = enrollmentDAO
                .recoverEnrollmentByPractitionerAndPeriod(STORED_PRACTITIONER_ID, NON_EXISTENT_PERIOD_ID);

        assertNull(recoveredEnrollment);
    }

    @Test
    void getEnrollmentsByGroup_ExistingGroup_ReturnsExpectedEnrollment() throws DAOException {
        List<GroupEnrollment> groupEnrollments = enrollmentDAO.getEnrollmentsByGroup(STORED_GROUP_ID);

        assertEquals(1, groupEnrollments.size());
        assertEquals(STORED_PRACTITIONER_ID, groupEnrollments.get(0).getPractitionerId());
    }

    @Test
    void insertEnrollment_DifferentPeriod_AllowsReEnrollment() throws DAOException, SQLException {
        createSecondPeriodAndGroup();
        GroupEnrollment reEnrollment = buildEnrollment(SECOND_GROUP_ID, SECOND_PERIOD_ID);

        int generatedId = enrollmentDAO.insertEnrollment(reEnrollment);

        assertTrue(generatedId > 0);
        assertEquals(2, enrollmentDAO.getEnrollmentsByPractitioner(STORED_PRACTITIONER_ID).size());
    }

    @Test
    void insertEnrollment_SamePeriod_ThrowsDAOException() {
        GroupEnrollment duplicatedEnrollment = buildEnrollment(STORED_GROUP_ID, STORED_PERIOD_ID);

        assertThrows(DAOException.class, () -> enrollmentDAO.insertEnrollment(duplicatedEnrollment));
    }

    private GroupEnrollment buildEnrollment(int groupId, int periodId) {
        GroupEnrollment enrollment = new GroupEnrollment();
        enrollment.setPractitionerId(STORED_PRACTITIONER_ID);
        enrollment.setGroupId(groupId);
        enrollment.setPeriodId(periodId);
        enrollment.setOpportunityNumber(FIRST_OPPORTUNITY);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        return enrollment;
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
