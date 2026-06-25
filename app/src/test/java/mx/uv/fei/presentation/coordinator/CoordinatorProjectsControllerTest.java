package mx.uv.fei.presentation.coordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.domain.manager.academic.ProjectManager;
import mx.uv.fei.presentation.shell.ShellNavigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorProjectsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorProjects.fxml";
    private static final int ORGANIZATION_COLUMN_INDEX = 1;
    private static final int AVAILABLE_COLUMN_INDEX = 3;
    private static final int STATUS_COLUMN_INDEX = 4;
    private static final int PROJECT_ID = 7;
    private static final int PARTICIPANT_CAPACITY = 2;
    private static final int ASSIGNED_COUNT = 1;

    private final ProjectManager projectManager = mock(ProjectManager.class);
    private final OrganizationManager organizationManager = mock(OrganizationManager.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(projectManager.getAllProjects()).thenReturn(List.of(buildProject()));
        when(projectManager.getAssignedCountsByProject()).thenReturn(Map.of(PROJECT_ID, ASSIGNED_COUNT));
        when(organizationManager.getAllOrganizations()).thenReturn(List.of(buildOrganization()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorProjectsController(
                projectManager, organizationManager, shellNavigator));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Project buildProject() {
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setProjectName("Sistema de Inventario");
        project.setCompanyId(1);
        project.setParticipantCapacity(PARTICIPANT_CAPACITY);
        project.setStatus("Active");

        return project;
    }

    private Organization buildOrganization() {
        Organization organization = new Organization();
        organization.setIdOrganization(1);
        organization.setNameOrganization("Tecnologias Web del Golfo");

        return organization;
    }

    private TableView<Project> projectsTable() {
        return lookup("#projectsTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_ActiveProject_ShowsActivoStatusLabel() {
        TableColumn<Project, String> statusColumn =
                (TableColumn<Project, String>) projectsTable().getColumns().get(STATUS_COLUMN_INDEX);

        assertEquals("Activo", statusColumn.getCellData(0));
    }

    @Test
    void initialize_ProjectWithCompany_ResolvesOrganizationName() {
        TableColumn<Project, String> organizationColumn =
                (TableColumn<Project, String>) projectsTable().getColumns().get(ORGANIZATION_COLUMN_INDEX);

        assertEquals("Tecnologias Web del Golfo", organizationColumn.getCellData(0));
    }

    @Test
    void initialize_ProjectWithAssignedPractitioners_ShowsAvailableSlots() {
        TableColumn<Project, String> availableColumn =
                (TableColumn<Project, String>) projectsTable().getColumns().get(AVAILABLE_COLUMN_INDEX);

        assertEquals(String.valueOf(PARTICIPANT_CAPACITY - ASSIGNED_COUNT), availableColumn.getCellData(0));
    }

    @Test
    void search_NonMatchingQuery_HidesProjectFromTable() {
        applySearchQuery("proyecto-inexistente");

        assertEquals(0, projectsTable().getItems().size());
    }

    private void applySearchQuery(String query) {
        TextField searchTextField = lookup("#searchTextField").queryAs(TextField.class);
        interact(() -> {
            searchTextField.setText(query);
            searchTextField.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED,
                    "", "", KeyCode.UNDEFINED, false, false, false, false));
        });
    }
}
