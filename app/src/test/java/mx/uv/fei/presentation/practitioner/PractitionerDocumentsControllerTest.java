package mx.uv.fei.presentation.practitioner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.manager.reporting.MonthlyReportManager;
import mx.uv.fei.domain.manager.reporting.PractitionerDocumentManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class PractitionerDocumentsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/practitionerDocuments.fxml";
    private static final int PRACTITIONER_ID = 123;

    private final PractitionerDocumentManager documentManager = mock(PractitionerDocumentManager.class);
    private final MonthlyReportManager reportManager = mock(MonthlyReportManager.class);
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
        when(reportManager.verifyHasAssignedProject(PRACTITIONER_ID)).thenReturn(true);
        when(documentManager.areAllInitialDocumentsAccepted(PRACTITIONER_ID)).thenReturn(true);
        when(documentManager.getPractitionerDocuments(anyInt(), any(DocumentCategory.class))).thenReturn(List.of());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new PractitionerDocumentsController(documentManager, reportManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void initialize_WithAssignedProjectAndAccess_RendersDocumentTables() {
        assertNotNull(lookup("#initialDocumentsTableView").queryAs(TableView.class));
        assertNotNull(lookup("#finalDocumentsTableView").queryAs(TableView.class));
    }
}
