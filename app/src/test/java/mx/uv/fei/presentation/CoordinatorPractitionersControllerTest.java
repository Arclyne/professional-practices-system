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
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorPractitionersControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorPractitioners.fxml";

    private final PractitionerManager practitionerManager = mock(PractitionerManager.class);
    private final AppStore store = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(practitionerManager.getAllPractitioners()).thenReturn(List.of(buildPractitioner()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorPractitionersController(practitionerManager, store));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Practitioner buildPractitioner() {
        Practitioner practitioner = new Practitioner();
        practitioner.setName("Ana Sofia");
        practitioner.setLastName("Robles");
        practitioner.setEnrollment("zS21010044");
        practitioner.setStatus(UserStatus.ACTIVE);

        return practitioner;
    }

    private TableView<Practitioner> practitionersTable() {
        return lookup("#practitionersTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_WithPractitioners_LoadsRowIntoTable() {
        assertEquals(1, practitionersTable().getItems().size());
    }

    @Test
    void search_NonMatchingQuery_HidesPractitionerFromTable() {
        applySearchQuery("inexistente-zzz");

        assertEquals(0, practitionersTable().getItems().size());
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
