package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPracticeGroupDAO groupDAO;

    private PracticeGroup testGroup;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testGroup = new PracticeGroup();
        testGroup.setSection("NRC-84932");
        testGroup.setProfessorId(68);
        testGroup.setPeriodId(5);
    }

    @Test
    void insertPracticeGroup_ValidGroup_ReturnsGeneratedId() throws DAOException {
        int resultId = groupDAO.insertPracticeGroup(testGroup);
        assertTrue(resultId > 0);
    }

    @Test
    void recoverPracticeGroup_ExistingId_ReturnsGroup() throws DAOException {
        PracticeGroup expectedGroup = new PracticeGroup();
        expectedGroup.setGroupId(6);
        expectedGroup.setSection("Seccion G");
        expectedGroup.setPeriodId(5);
        expectedGroup.setProfessorId(68);

        PracticeGroup recovered = groupDAO.recoverPracticeGroup(6);
        assertEquals(expectedGroup, recovered);
    }

    @Test
    void getAllPracticeGroups_WithExistingData_ReturnsList() throws DAOException {
        List<PracticeGroup> resultList = groupDAO.getAllPracticeGroups();
        assertFalse(resultList.isEmpty());
    }

    @Test
    void updatePracticeGroup_ValidModifiedData_ReturnsTrue() throws DAOException {
        testGroup.setSection("NRC-99999");
        boolean isUpdated = groupDAO.updatePracticeGroup(testGroup, 6);
        assertTrue(isUpdated);
    }

    @Test
    void insertPracticeGroup_NonExistentProfessor_ThrowsDAOException() {
        testGroup.setProfessorId(9999);
        assertThrows(DAOException.class, () -> {
            groupDAO.insertPracticeGroup(testGroup);
        });
    }

    @Test
    void recoverPracticeGroup_NonExistentId_ReturnsEmptyGroup() throws DAOException {
        PracticeGroup expectedEmpty = new PracticeGroup();
        PracticeGroup recovered = groupDAO.recoverPracticeGroup(9999);
        assertEquals(expectedEmpty, recovered);
    }

    @Test
    void updatePracticeGroup_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = groupDAO.updatePracticeGroup(testGroup, 9999);
        assertFalse(isUpdated);
    }
}