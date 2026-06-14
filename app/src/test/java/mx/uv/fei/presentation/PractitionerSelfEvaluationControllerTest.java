package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.manager.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class PractitionerSelfEvaluationControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/practitionerSelfEvaluation.fxml";
    private static final int PRACTITIONER_ID = 123;
    private static final int FINAL_REPORT_ID = 2;

    private final SelfEvaluationManager selfEvaluationManager = mock(SelfEvaluationManager.class);
    private final ProgressReportManager progressReportManager = mock(ProgressReportManager.class);
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
        when(progressReportManager.getProgressReportsByPractitioner(PRACTITIONER_ID))
                .thenReturn(List.of(buildDeliveredFinalReport()));
        when(selfEvaluationManager.recoverSelfEvaluation(anyInt())).thenReturn(null);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new PractitionerSelfEvaluationController(selfEvaluationManager, progressReportManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private ProgressReport buildDeliveredFinalReport() {
        ProgressReport finalReport = new ProgressReport();
        finalReport.setReportId(FINAL_REPORT_ID);
        finalReport.setReportType("Final");
        finalReport.setStatus(ReportStatus.SUBMITTED.getDatabaseValue());
        return finalReport;
    }

    private void clickSaveButton() {
        interact(() -> lookup("#saveButton").queryAs(Button.class).fire());
    }

    @Test
    void handleSaveEvaluation_DeliveredFinalReport_RegistersSelfEvaluation() throws Exception {
        clickSaveButton();

        verify(selfEvaluationManager).registerSelfEvaluation(any());
    }
}
