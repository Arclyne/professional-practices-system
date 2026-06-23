package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.presentation.shell.ShellNavigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorOrganizationsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorOrganizations.fxml";
    private static final int STATUS_COLUMN_INDEX = 3;

    private final OrganizationManager organizationManager = mock(OrganizationManager.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(organizationManager.getAllOrganizations()).thenReturn(List.of(buildOrganization()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorOrganizationsController(organizationManager, shellNavigator));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Organization buildOrganization() {
        Organization organization = new Organization();
        organization.setNameOrganization("Tecnologias Web del Golfo");
        organization.setBusiness("Technology");
        organization.setMail("contacto@tecgolfo.mx");
        organization.setState("Active");

        return organization;
    }

    private TableView<Organization> organizationsTable() {
        return lookup("#organizationsTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_ActiveOrganization_ShowsActivoStatusLabel() {
        TableColumn<Organization, String> statusColumn =
                (TableColumn<Organization, String>) organizationsTable().getColumns().get(STATUS_COLUMN_INDEX);

        assertEquals("Activo", statusColumn.getCellData(0));
    }

    @Test
    void search_NonMatchingQuery_HidesOrganizationFromTable() {
        applySearchQuery("inexistente-zzz");

        assertEquals(0, organizationsTable().getItems().size());
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
