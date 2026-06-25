package mx.uv.fei.domain.manager.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PostulationManagerTest {

    private static final int STORED_PRACTITIONER_ID = 123;
    private static final int ASSIGNED_PROJECT_ID = 1;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private PostulationManager postulationManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void retrievePractitionerPostulations_ValidPractitioner_ReturnsExpectedList() throws ManagerException {
        List<ProjectPostulation> expectedPostulations = new ArrayList<>();
        ProjectPostulation assignedPostulation = new ProjectPostulation();
        assignedPostulation.setPractitionerId(STORED_PRACTITIONER_ID);
        assignedPostulation.setProjectId(ASSIGNED_PROJECT_ID);
        assignedPostulation.setProjectName("Sistema de Inventario Web");
        assignedPostulation.setPostulationStatus("Assigned");
        assignedPostulation.setPriorityLevel(1);
        expectedPostulations.add(assignedPostulation);

        List<ProjectPostulation> resultPostulations =
                postulationManager.retrievePractitionerPostulations(STORED_PRACTITIONER_ID);

        assertEquals(expectedPostulations, resultPostulations);
    }

    @Test
    void registerPractitionerPriorities_EmptyList_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> postulationManager.registerPractitionerPriorities(STORED_PRACTITIONER_ID, List.of()));
    }

    @Test
    void assignProjectToPractitioner_ValidIds_DoesNotThrow() {
        assertDoesNotThrow(() -> postulationManager.assignProjectToPractitioner(STORED_PRACTITIONER_ID, ASSIGNED_PROJECT_ID));
    }

    @Test
    void retrieveAllAvailableProjects_ReturnsExpectedList() throws ManagerException {
        List<Project> expectedProjects = new ArrayList<>();

        Project inventoryProject = new Project();
        inventoryProject.setProjectId(1);
        inventoryProject.setProjectName("Sistema de Inventario Web");
        inventoryProject.setDescription("Desarrollo de un sistema web para el control de inventario");
        inventoryProject.setParticipantCapacity(2);
        inventoryProject.setManagerId(1);
        inventoryProject.setStatus("Active");
        inventoryProject.setStartDate(Date.valueOf("2026-01-01"));
        inventoryProject.setEndDate(Date.valueOf("2026-06-01"));
        inventoryProject.setCompanyId(1);
        expectedProjects.add(inventoryProject);

        Project salesProject = new Project();
        salesProject.setProjectId(2);
        salesProject.setProjectName("Aplicacion Movil de Ventas");
        salesProject.setDescription("Desarrollo de una aplicacion movil para la gestion de ventas");
        salesProject.setParticipantCapacity(3);
        salesProject.setManagerId(2);
        salesProject.setStatus("Active");
        salesProject.setStartDate(Date.valueOf("2026-01-01"));
        salesProject.setEndDate(Date.valueOf("2026-06-01"));
        salesProject.setCompanyId(2);
        expectedProjects.add(salesProject);

        Project humanResourcesProject = new Project();
        humanResourcesProject.setProjectId(3);
        humanResourcesProject.setProjectName("Portal de Recursos Humanos");
        humanResourcesProject.setDescription("Mantenimiento del portal interno de recursos humanos");
        humanResourcesProject.setParticipantCapacity(1);
        humanResourcesProject.setManagerId(3);
        humanResourcesProject.setStatus("Active");
        humanResourcesProject.setStartDate(Date.valueOf("2026-01-01"));
        humanResourcesProject.setEndDate(Date.valueOf("2026-06-01"));
        humanResourcesProject.setCompanyId(3);
        expectedProjects.add(humanResourcesProject);

        List<Project> resultProjects = postulationManager.retrieveAllAvailableProjects();

        assertEquals(expectedProjects, resultProjects);
    }
}
