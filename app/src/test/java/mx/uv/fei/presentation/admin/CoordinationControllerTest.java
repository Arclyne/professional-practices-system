package mx.uv.fei.presentation.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.CoordinatorManager;
import mx.uv.fei.presentation.shell.ShellNavigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinationControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordination.fxml";

    private final CoordinatorManager coordinatorManager = mock(CoordinatorManager.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        Coordinator activeCoordinator = buildActiveCoordinator();
        when(coordinatorManager.retrieveCurrentCoordinator()).thenReturn(activeCoordinator);
        when(coordinatorManager.getAllCoordinators()).thenReturn(List.of(activeCoordinator));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinationController(coordinatorManager, shellNavigator));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Coordinator buildActiveCoordinator() {
        Coordinator coordinator = new Coordinator();
        coordinator.setId(67);
        coordinator.setName("Marco Antonio");
        coordinator.setLastName("Rodriguez Castillo");
        coordinator.setUserName("30022222");
        coordinator.setEmail("mrodriguez@uv.mx");
        coordinator.setStatus(UserStatus.ACTIVE);
        coordinator.setGender(Gender.MALE);

        return coordinator;
    }

    @Test
    void initialize_ActiveCoordinatorExists_DisablesRegisterButton() {
        Button registerButton = lookup("#registerButton").queryAs(Button.class);

        assertTrue(registerButton.isDisabled());
    }

    @Test
    void initialize_ActiveCoordinatorExists_ShowsActiveCard() {
        VBox activeCoordinatorCard = lookup("#activeCoordinatorCard").queryAs(VBox.class);

        assertTrue(activeCoordinatorCard.isVisible());
    }

    @Test
    void initialize_ActiveCoordinatorExists_LoadsCoordinatorIntoTable() {
        TableView<Coordinator> coordinatorsTableView = lookup("#coordinatorsTableView").queryAs(TableView.class);

        assertEquals(1, coordinatorsTableView.getItems().size());
    }

    @Test
    void initialize_ActiveCoordinatorExists_DisablesActivateButton() {
        Button activateButton = lookup("#activateButton").queryAs(Button.class);

        assertTrue(activateButton.isDisabled());
    }

    @Test
    void search_MatchingQuery_KeepsCoordinatorInTable() {
        TableView<Coordinator> coordinatorsTableView = lookup("#coordinatorsTableView").queryAs(TableView.class);

        applySearchQuery("Marco");

        assertEquals(1, coordinatorsTableView.getItems().size());
    }

    @Test
    void search_ByUsername_KeepsCoordinatorInTable() {
        TableView<Coordinator> coordinatorsTableView = lookup("#coordinatorsTableView").queryAs(TableView.class);

        applySearchQuery("30022222");

        assertEquals(1, coordinatorsTableView.getItems().size());
    }

    @Test
    void search_NonMatchingQuery_HidesCoordinatorFromTable() {
        TableView<Coordinator> coordinatorsTableView = lookup("#coordinatorsTableView").queryAs(TableView.class);

        applySearchQuery("inexistente-zzz");

        assertEquals(0, coordinatorsTableView.getItems().size());
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
