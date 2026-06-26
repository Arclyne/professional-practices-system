package mx.uv.fei.presentation.practitioner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.enums.SelfEvaluationStatus;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.manager.evaluation.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.presentation.shell.ShellNavigator;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class PractitionerSelfEvaluationViewTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/practitionerSelfEvaluation.fxml";
    private static final int PRACTITIONER_ID = 123;
    private static final int FINAL_REPORT_ID = 2;
    private static final int Q1_VALUE = 4;
    private static final int Q10_VALUE = 5;

    private final SelfEvaluationManager selfEvaluationManager = mock(SelfEvaluationManager.class);
    private final ProgressReportManager progressReportManager = mock(ProgressReportManager.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);
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
                .thenReturn(List.of(buildAcceptedFinalReport()));
        when(selfEvaluationManager.recoverSelfEvaluation(anyInt())).thenReturn(buildReviewedEvaluation());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new PractitionerSelfEvaluationController(selfEvaluationManager, progressReportManager,
                        shellNavigator, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private ProgressReport buildAcceptedFinalReport() {
        ProgressReport finalReport = new ProgressReport();
        finalReport.setReportId(FINAL_REPORT_ID);
        finalReport.setReportType("Final");
        finalReport.setStatus(ReportStatus.EVALUATED.getDatabaseValue());
        return finalReport;
    }

    private SelfEvaluation buildReviewedEvaluation() {
        SelfEvaluation evaluation = new SelfEvaluation();
        evaluation.setSelfEvalId(8);
        evaluation.setPractitionerId(PRACTITIONER_ID);
        evaluation.setReportId(FINAL_REPORT_ID);
        evaluation.setStatus(SelfEvaluationStatus.REVIEWED.getDatabaseValue());
        evaluation.setEvidence("/Users/practicante/SimuladorOneDrive_FEI/evidencia.pdf");
        evaluation.setQ1(Q1_VALUE);
        evaluation.setQ2(3);
        evaluation.setQ3(3);
        evaluation.setQ4(3);
        evaluation.setQ5(3);
        evaluation.setQ6(3);
        evaluation.setQ7(3);
        evaluation.setQ8(3);
        evaluation.setQ9(3);
        evaluation.setQ10(Q10_VALUE);
        return evaluation;
    }

    @SuppressWarnings("unchecked")
    private int comboValue(String nodeId) {
        return lookup(nodeId).queryAs(ComboBox.class).getValue() == null
                ? -1
                : (Integer) lookup(nodeId).queryAs(ComboBox.class).getValue();
    }

    private boolean isButtonDisabled(String nodeId) {
        return lookup(nodeId).queryAs(Button.class).isDisabled();
    }

    @Test
    void initialize_ExistingEvaluation_ShowsSavedAnswers() {
        assertEquals(Q1_VALUE, comboValue("#q1ComboBox"));
        assertEquals(Q10_VALUE, comboValue("#q10ComboBox"));
    }

    @Test
    void initialize_EvaluationWithUploadedEvidence_EnablesViewEvidenceButton() {
        assertFalse(isButtonDisabled("#viewEvidenceButton"));
    }
}
