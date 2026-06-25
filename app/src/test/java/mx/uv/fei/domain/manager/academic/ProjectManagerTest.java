package mx.uv.fei.domain.manager.academic;

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
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ProjectManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ProjectManager projectManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewProject_ValidData_DoesNotThrow() {
        Project newProject = new Project();
        newProject.setProjectName("Plataforma de Encuestas Institucionales");
        newProject.setCompanyId(1);
        newProject.setManagerId(1);
        newProject.setStartDate(Date.valueOf("2026-01-01"));
        newProject.setEndDate(Date.valueOf("2026-06-01"));

        assertDoesNotThrow(() -> projectManager.registerNewProject(newProject));
    }

    @Test
    void getAllProjects_ReturnsExpectedList() throws ManagerException {
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

        List<Project> resultProjects = projectManager.getAllProjects();

        assertEquals(expectedProjects, resultProjects);
    }

    @Test
    void inactivateMultipleProjects_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> projectManager.inactivateMultipleProjects(List.of(1)));
    }

    @Test
    void inactivateProject_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> projectManager.inactivateProject(1));
    }

    @Test
    void activateProject_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> projectManager.activateProject(1));
    }
}
