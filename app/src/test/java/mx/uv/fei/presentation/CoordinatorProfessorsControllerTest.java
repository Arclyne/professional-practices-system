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

import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorProfessorsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorProfessors.fxml";

    private final ProfessorManager professorManager = mock(ProfessorManager.class);
    private final AppStore store = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(professorManager.getAllProfessors()).thenReturn(List.of(buildProfessor()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorProfessorsController(professorManager, store));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setName("Raul");
        professor.setLastName("Beltran Ng");
        professor.setUserName("P-10245");
        professor.setEmail("rbeltran@uv.mx");
        professor.setStatus(UserStatus.ACTIVE);

        return professor;
    }

    private TableView<Professor> professorsTable() {
        return lookup("#professorsTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_WithProfessors_LoadsRowIntoTable() {
        assertEquals(1, professorsTable().getItems().size());
    }

    @Test
    void search_NonMatchingQuery_HidesProfessorFromTable() {
        applySearchQuery("inexistente-zzz");

        assertEquals(0, professorsTable().getItems().size());
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
