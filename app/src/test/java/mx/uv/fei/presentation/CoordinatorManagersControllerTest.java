package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.manager.ManagerManager;
import mx.uv.fei.domain.manager.OrganizationManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorManagersControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorManagers.fxml";
    private static final int ORGANIZATION_COLUMN_INDEX = 3;
    private static final int STATUS_COLUMN_INDEX = 4;

    private final ManagerManager managerManager = mock(ManagerManager.class);
    private final OrganizationManager organizationManager = mock(OrganizationManager.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(managerManager.getAllManagers()).thenReturn(List.of(buildManager()));
        when(organizationManager.getAllOrganizations()).thenReturn(List.of(buildOrganization()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorManagersController(
                managerManager, organizationManager));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Manager buildManager() {
        Manager manager = new Manager();
        manager.setId(1);
        manager.setName("Roberto Sanchez Luna");
        manager.setPhone("2281234567");
        manager.setEmail("rsanchez@tecgolfo.mx");
        manager.setOrganizationId(1);
        manager.setStatus(UserStatus.ACTIVE);

        return manager;
    }

    private Organization buildOrganization() {
        Organization organization = new Organization();
        organization.setIdOrganization(1);
        organization.setNameOrganization("Tecnologias Web del Golfo");

        return organization;
    }

    private TableView<Manager> managersTable() {
        return lookup("#managersTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_ManagerWithOrganization_ResolvesOrganizationName() {
        TableColumn<Manager, String> organizationColumn =
                (TableColumn<Manager, String>) managersTable().getColumns().get(ORGANIZATION_COLUMN_INDEX);

        assertEquals("Tecnologias Web del Golfo", organizationColumn.getCellData(0));
    }

    @Test
    void initialize_ActiveManager_ShowsActivoStatusLabel() {
        TableColumn<Manager, String> statusColumn =
                (TableColumn<Manager, String>) managersTable().getColumns().get(STATUS_COLUMN_INDEX);

        assertEquals("Activo", statusColumn.getCellData(0));
    }

    @Test
    void initialize_LoadsManagers_ShowsListAndHidesForm() {
        VBox listPane = lookup("#listPane").queryAs(VBox.class);
        VBox formPane = lookup("#formPane").queryAs(VBox.class);

        assertTrue(listPane.isVisible());
        assertFalse(formPane.isVisible());
        assertEquals(1, managersTable().getItems().size());
    }

    @Test
    void search_NonMatchingQuery_HidesManagerFromTable() {
        applySearchQuery("inexistente-zzz");

        assertEquals(0, managersTable().getItems().size());
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
