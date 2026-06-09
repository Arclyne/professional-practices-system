package mx.uv.fei.dataaccess.repositories;

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
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")

public class PostulationDAOTest {

    private static final int MISS_PRACTITIONER_ID = 99999;
    private static final int PRACTITIONER_ID = 123;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPostulationDAO postulationDAO;

    private List<Project> prioritizedProjects;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        Project project1 = new Project();
        project1.setProjectId(1);

        Project project2 = new Project();
        project2.setProjectId(2);

        prioritizedProjects = List.of(project1, project2);
    }

    @Test
    void hasPractitionerSubmittedPriorities_WithNoPriorSubmission_ReturnsFalse() throws DAOException {
        boolean hasSubmitted = postulationDAO.hasPractitionerSubmittedPriorities(MISS_PRACTITIONER_ID);
        assertFalse(hasSubmitted);
    }

    @Test
    void insertProjectPriorities_ValidProjects_ReturnsTrue() throws DAOException {
        boolean isInserted = postulationDAO.insertProjectPriorities(PRACTITIONER_ID, prioritizedProjects);
        assertTrue(isInserted);
    }

    @Test
    void retrievePractitionerPostulations_WithExistingPostulations_ReturnsList() throws DAOException {
        postulationDAO.insertProjectPriorities(PRACTITIONER_ID, prioritizedProjects);
        List<ProjectPostulation> resultList = postulationDAO.retrievePractitionerPostulations(PRACTITIONER_ID);
        assertFalse(resultList.isEmpty());
    }

    @Test
    void assignProjectUsingStoredProcedure_ValidIds_ReturnsTrue() throws DAOException {
        postulationDAO.insertProjectPriorities(PRACTITIONER_ID, prioritizedProjects);
        boolean isAssigned = postulationDAO.assignProjectUsingStoredProcedure(PRACTITIONER_ID, 1);
        assertTrue(isAssigned);
    }

    @Test
    void insertProjectPriorities_NonExistentPractitioner_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> {
            postulationDAO.insertProjectPriorities(9999, prioritizedProjects);
        });
    }

    @Test
    void retrievePractitionerPostulations_NonExistentPractitioner_ReturnsEmptyList() throws DAOException {
        List<ProjectPostulation> resultList = postulationDAO.retrievePractitionerPostulations(9999);
        assertTrue(resultList.isEmpty());
    }
}
