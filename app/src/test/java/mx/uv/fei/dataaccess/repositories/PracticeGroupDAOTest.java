package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.PracticeGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PracticeGroupDAOTest {

    private static final int STORED_GROUP_ID = 6;
    private static final int STORED_PROFESSOR_ID = 68;
    private static final int STORED_PERIOD_ID = 5;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPracticeGroupDAO groupDAO;

    private PracticeGroup newGroup;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newGroup = new PracticeGroup();
        newGroup.setSection("NRC-84932");
        newGroup.setProfessorId(STORED_PROFESSOR_ID);
        newGroup.setPeriodId(STORED_PERIOD_ID);
    }

    private PracticeGroup buildStoredGroup() {
        PracticeGroup storedGroup = new PracticeGroup();
        storedGroup.setGroupId(STORED_GROUP_ID);
        storedGroup.setSection("Seccion 601");
        storedGroup.setPeriodId(STORED_PERIOD_ID);
        storedGroup.setProfessorId(STORED_PROFESSOR_ID);
        return storedGroup;
    }

    @Test
    void insertPracticeGroup_ValidGroup_ReturnsGeneratedId() throws DAOException {
        int resultId = groupDAO.insertPracticeGroup(newGroup);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverPracticeGroup_ExistingId_ReturnsGroup() throws DAOException {
        PracticeGroup expectedGroup = buildStoredGroup();

        PracticeGroup recoveredGroup = groupDAO.recoverPracticeGroup(STORED_GROUP_ID);

        assertEquals(expectedGroup, recoveredGroup);
    }

    @Test
    void getAllPracticeGroups_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<PracticeGroup> expectedGroups = new ArrayList<>();
        expectedGroups.add(buildStoredGroup());

        List<PracticeGroup> resultGroups = groupDAO.getAllPracticeGroups();

        assertEquals(expectedGroups, resultGroups);
    }

    @Test
    void updatePracticeGroup_ValidModifiedData_DoesNotThrow() throws DAOException {
        newGroup.setSection("NRC-99999");

        assertDoesNotThrow(() -> groupDAO.updatePracticeGroup(newGroup, STORED_GROUP_ID));
    }

    @Test
    void insertPracticeGroup_NonExistentProfessor_ThrowsDAOException() {
        newGroup.setProfessorId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> groupDAO.insertPracticeGroup(newGroup));
    }

    @Test
    void recoverPracticeGroup_NonExistentId_ReturnsEmptyGroup() throws DAOException {
        PracticeGroup recoveredGroup = groupDAO.recoverPracticeGroup(NON_EXISTENT_ID);

        assertEquals(new PracticeGroup(), recoveredGroup);
    }

    @Test
    void updatePracticeGroup_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> groupDAO.updatePracticeGroup(newGroup, NON_EXISTENT_ID));
    }
}
