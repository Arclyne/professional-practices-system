package mx.uv.fei.presentation.professor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.reporting.PractitionerDocumentManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class ProfessorDocumentsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/professorDocuments.fxml";
    private static final int PROFESSOR_ID = 68;

    private final PractitionerDocumentManager documentManager = mock(PractitionerDocumentManager.class);
    private final PractitionerManager practitionerManager = mock(PractitionerManager.class);
    private final PracticeGroupManager practiceGroupManager = mock(PracticeGroupManager.class);
    private final PeriodManager periodManager = mock(PeriodManager.class);
    private final AppStore appStore = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        User professor = new User();
        professor.setId(PROFESSOR_ID);
        when(appStore.getState()).thenReturn(RootState.initialState().withSessionState(new SessionState(professor)));
        when(practiceGroupManager.getGroupsByProfessorAndPeriod(anyInt(), anyInt())).thenReturn(List.of());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new ProfessorDocumentsController(documentManager, practitionerManager, practiceGroupManager,
                        periodManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void initialize_WithoutPractitioners_RendersPractitionerList() {
        assertNotNull(lookup("#practitionersListView").queryAs(ListView.class));
    }
}
