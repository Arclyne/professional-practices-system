package mx.uv.fei.presentation.practitioner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.sql.Date;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.manager.academic.PostulationManager;
import mx.uv.fei.domain.manager.people.ManagerManager;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class ViewAssignedProjectControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/viewAssignedProject.fxml";
    private static final int PRACTITIONER_ID = 123;
    private static final int PROJECT_ID = 9;
    private static final int COMPANY_ID = 4;
    private static final int MANAGER_ID = 6;

    private final PostulationManager postulationManager = mock(PostulationManager.class);
    private final OrganizationManager organizationManager = mock(OrganizationManager.class);
    private final ManagerManager managerManager = mock(ManagerManager.class);
    private final AppStore appStore = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        User practitioner = new User();
        practitioner.setId(PRACTITIONER_ID);
        when(appStore.getState()).thenReturn(RootState.initialState().withSessionState(new SessionState(practitioner)));
        when(postulationManager.getAssignedProject(PRACTITIONER_ID)).thenReturn(buildProject());
        when(organizationManager.getOrganizationById(COMPANY_ID)).thenReturn(buildOrganization());
        when(managerManager.getManagerById(MANAGER_ID)).thenReturn(buildManager());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new ViewAssignedProjectController(
                postulationManager, organizationManager, managerManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Project buildProject() {
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setProjectName("Sistema de inventario");
        project.setStatus("Activo");
        project.setParticipantCapacity(3);
        project.setStartDate(Date.valueOf("2026-02-01"));
        project.setEndDate(Date.valueOf("2026-07-01"));
        project.setDescription("Desarrollo de un sistema de inventario.");
        project.setCompanyId(COMPANY_ID);
        project.setManagerId(MANAGER_ID);
        return project;
    }

    private Organization buildOrganization() {
        Organization organization = new Organization();
        organization.setIdOrganization(COMPANY_ID);
        organization.setNameOrganization("Tecnologías FEI");
        organization.setCity("Xalapa");
        organization.setAdress("Av. Universidad 100");
        organization.setBusiness("Software");
        organization.setMail("contacto@feitec.mx");
        organization.setCellphone("2281234567");
        return organization;
    }

    private Manager buildManager() {
        Manager manager = new Manager();
        manager.setId(MANAGER_ID);
        manager.setName("Laura Méndez");
        manager.setEmail("laura@feitec.mx");
        manager.setPhone("2287654321");
        return manager;
    }

    private String labelText(String nodeId) {
        return lookup(nodeId).queryAs(Label.class).getText();
    }

    @Test
    void initialize_AssignedProject_ShowsOrganizationName() {
        assertTrue(labelText("#organizationNameLabel").contains("Tecnologías FEI"));
    }

    @Test
    void initialize_AssignedProject_ShowsManagerName() {
        assertTrue(labelText("#managerNameLabel").contains("Laura Méndez"));
    }

    @Test
    void initialize_AssignedProject_ShowsProjectCapacity() {
        assertTrue(labelText("#projectCapacityLabel").contains("3"));
    }
}
