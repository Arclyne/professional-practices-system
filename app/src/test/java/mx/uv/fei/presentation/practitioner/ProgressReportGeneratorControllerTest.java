package mx.uv.fei.presentation.practitioner;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.manager.infrastructure.CloudStorageManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.common.ReportPdfGenerator;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class ProgressReportGeneratorControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/progressReportGenerator.fxml";
    private static final String TYPE_FINAL_LABEL = "Final (mín. 420 horas)";
    private static final int PRACTITIONER_ID = 123;
    private static final LocalDate PERIOD_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2026, 11, 30);

    private final ProgressReportManager progressReportManager = mock(ProgressReportManager.class);
    private final CloudStorageManager cloudStorageManager = mock(CloudStorageManager.class);
    private final ReportPdfGenerator pdfGenerator = mock(ReportPdfGenerator.class);
    private final PeriodManager periodManager = mock(PeriodManager.class);
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
        when(periodManager.getActivePeriod()).thenReturn(buildActivePeriod());
        when(progressReportManager.getProgressReportsByPractitioner(PRACTITIONER_ID)).thenReturn(new ArrayList<>());
        when(progressReportManager.generateProgressReport(anyInt(), any(), any(), any()))
                .thenReturn(buildGeneratedReport());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new ProgressReportGeneratorController(
                progressReportManager, cloudStorageManager, pdfGenerator, periodManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Period buildActivePeriod() {
        Period activePeriod = new Period();
        activePeriod.setStartDate(Date.valueOf(PERIOD_START));
        activePeriod.setEndDate(Date.valueOf(PERIOD_END));
        return activePeriod;
    }

    private ProgressReport buildGeneratedReport() {
        ProgressReport generatedReport = new ProgressReport();
        generatedReport.setReportType("Intermedio");
        generatedReport.setStatus("Pendiente de Firma");
        generatedReport.setTotalHoursAtSubmission(215.0);
        return generatedReport;
    }

    private void selectIntermediateCutoffDate() {
        interact(() -> lookup("#periodEndDatePicker").queryAs(DatePicker.class).setValue(PERIOD_END));
    }

    private void clickGenerateButton() {
        interact(() -> lookup("#generateReportButton").queryAs(Button.class).fire());
    }

    @Test
    void handleGenerateReport_IntermediateTypeSelected_GeneratesIntermediateReport() throws Exception {
        selectIntermediateCutoffDate();

        clickGenerateButton();

        verify(progressReportManager).generateProgressReport(
                eq(PRACTITIONER_ID), eq(ProgressReportType.INTERMEDIO), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleGenerateReport_FinalTypeSelected_GeneratesFinalReport() throws Exception {
        interact(() -> lookup("#reportTypeComboBox").queryAs(ComboBox.class).setValue(TYPE_FINAL_LABEL));

        clickGenerateButton();

        verify(progressReportManager).generateProgressReport(
                eq(PRACTITIONER_ID), eq(ProgressReportType.FINAL), any(), any());
    }

    @Test
    void handleGenerateReport_IntermediateWithoutCutoffDate_DoesNotGenerateReport() throws Exception {
        clickGenerateButton();

        verify(progressReportManager, never()).generateProgressReport(anyInt(), any(), any(), any());
    }
}
