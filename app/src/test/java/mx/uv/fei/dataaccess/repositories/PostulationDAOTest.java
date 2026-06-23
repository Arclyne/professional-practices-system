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
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PostulationDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int ASSIGNED_PROJECT_ID = 1;
    private static final int NON_EXISTENT_PRACTITIONER_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPostulationDAO postulationDAO;

    private List<Project> prioritizedProjects;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        Project inventoryProject = new Project();
        inventoryProject.setProjectId(1);

        Project salesProject = new Project();
        salesProject.setProjectId(2);

        prioritizedProjects = List.of(inventoryProject, salesProject);
    }

    @Test
    void hasPractitionerSubmittedPriorities_WithNoPriorSubmission_ReturnsFalse() throws DAOException {
        boolean hasSubmitted = postulationDAO.hasPractitionerSubmittedPriorities(NON_EXISTENT_PRACTITIONER_ID);

        assertFalse(hasSubmitted);
    }

    @Test
    void insertProjectPriorities_ValidProjects_DoesNotThrow() throws DAOException {
        assertDoesNotThrow(() -> postulationDAO.insertProjectPriorities(PRACTITIONER_ID, prioritizedProjects));
    }

    @Test
    void retrievePractitionerPostulations_WithExistingPostulations_ReturnsExpectedList() throws DAOException {
        List<ProjectPostulation> expectedPostulations = new ArrayList<>();
        ProjectPostulation assignedPostulation = new ProjectPostulation();
        assignedPostulation.setPractitionerId(PRACTITIONER_ID);
        assignedPostulation.setProjectId(ASSIGNED_PROJECT_ID);
        assignedPostulation.setProjectName("Sistema de Inventario Web");
        assignedPostulation.setPostulationStatus("Assigned");
        assignedPostulation.setPriorityLevel(1);
        expectedPostulations.add(assignedPostulation);

        List<ProjectPostulation> resultPostulations = postulationDAO.retrievePractitionerPostulations(PRACTITIONER_ID);

        assertEquals(expectedPostulations, resultPostulations);
    }

    @Test
    void assignProjectUsingStoredProcedure_ValidIds_DoesNotThrow() throws DAOException {
        postulationDAO.insertProjectPriorities(PRACTITIONER_ID, prioritizedProjects);

        assertDoesNotThrow(() -> postulationDAO.assignProjectUsingStoredProcedure(PRACTITIONER_ID, ASSIGNED_PROJECT_ID));
    }

    @Test
    void insertProjectPriorities_NonExistentPractitioner_ThrowsDAOException() {
        assertThrows(DAOException.class,
                () -> postulationDAO.insertProjectPriorities(NON_EXISTENT_PRACTITIONER_ID, prioritizedProjects));
    }

    @Test
    void retrievePractitionerPostulations_NonExistentPractitioner_ReturnsEmptyList() throws DAOException {
        List<ProjectPostulation> resultPostulations =
                postulationDAO.retrievePractitionerPostulations(NON_EXISTENT_PRACTITIONER_ID);

        assertEquals(new ArrayList<>(), resultPostulations);
    }
}
