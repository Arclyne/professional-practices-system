package mx.uv.fei.presentation.professor;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.EvaluableReport;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.reporting.MonthlyReportManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class ProfessorEvaluateReportController implements Initializable {

    private static final String EVALUATED_PREFIX = "[Evaluado] ";
    private static final String PENDING_PREFIX = "[Pendiente] ";

    @FXML private ListView<EvaluableReport> reportsListView;
    @FXML private VBox evaluationContainer;
    @FXML private Label labelReportKind;
    @FXML private Label labelReportInfo;
    @FXML private Button btnViewPdf;
    @FXML private TextField fieldGrade;
    @FXML private TextArea areaFeedback;
    @FXML private Button btnSaveEvaluation;

    private final MonthlyReportManager monthlyReportManager;
    private final ProgressReportManager progressReportManager;
    private final PeriodManager periodManager;
    private final AppStore store;

    private EvaluableReport selectedReport;
    private int professorId;
    private int activePeriodId;

    @Inject
    public ProfessorEvaluateReportController(MonthlyReportManager monthlyReportManager,
                                             ProgressReportManager progressReportManager, PeriodManager periodManager,
                                             AppStore store) {
        this.monthlyReportManager = monthlyReportManager;
        this.progressReportManager = progressReportManager;
        this.periodManager = periodManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showEvaluationContainer(false);
        resolveProfessorContext();
        configureListView();
        loadAllSubmittedReports();
    }

    private void resolveProfessorContext() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        try {
            Period activePeriod = periodManager.getActivePeriod();
            activePeriodId = activePeriod != null ? activePeriod.getPeriodId() : 0;
        } catch (ManagerException e) {
            activePeriodId = 0;
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.WARNING);
        }
    }

    private void configureListView() {
        reportsListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(EvaluableReport report, boolean isEmpty) {
                super.updateItem(report, isEmpty);
                if (isEmpty || report == null) {
                    setText(null);
                } else {
                    setText(buildReportDisplayText(report));
                }
            }
        });

        reportsListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selectedReport) -> {
                    if (selectedReport != null) {
                        showReportDetails(selectedReport);
                    }
                });
    }

    private String buildReportDisplayText(EvaluableReport report) {
        boolean isEvaluated = ReportStatus.EVALUATED.getDatabaseValue().equals(report.getStatus());
        String prefix = isEvaluated ? EVALUATED_PREFIX : PENDING_PREFIX;
        return prefix + report.getDisplayName();
    }

    private void loadAllSubmittedReports() {
        List<EvaluableReport> allReports = new ArrayList<>();
        loadMonthlyReports(allReports);
        loadProgressReports(allReports);
        reportsListView.setItems(FXCollections.observableArrayList(allReports));
    }

    private void loadMonthlyReports(List<EvaluableReport> targetReports) {
        try {
            List<MonthlyReport> monthlyReports = monthlyReportManager
                    .getReportsForEvaluation(professorId, activePeriodId);
            for (MonthlyReport report : monthlyReports) {
                targetReports.add(EvaluableReport.fromMonthlyReport(report));
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", "Reportes mensuales: " + e.getMessage(), AlertType.WARNING);
        }
    }

    private void loadProgressReports(List<EvaluableReport> targetReports) {
        try {
            List<ProgressReport> progressReports = progressReportManager
                    .getSubmittedProgressReports(professorId, activePeriodId);
            for (ProgressReport report : progressReports) {
                targetReports.add(EvaluableReport.fromProgressReport(report));
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", "Reportes de avance: " + e.getMessage(), AlertType.WARNING);
        }
    }

    private void showReportDetails(EvaluableReport report) {
        selectedReport = report;
        showEvaluationContainer(true);

        labelReportKind.setText("Tipo de reporte: " + report.getReportKind());
        labelReportInfo.setText(report.getDisplayName() + "\nEstado: " + report.getStatus());

        boolean hasPdf = report.getSignedFileUrl() != null && !report.getSignedFileUrl().isEmpty();
        btnViewPdf.setDisable(!hasPdf);

        fillGradeField(report);
        fillFeedbackArea(report);
    }

    private void fillGradeField(EvaluableReport report) {
        if (report.getGrade() != null) {
            fieldGrade.setText(String.valueOf(report.getGrade()));
        } else {
            fieldGrade.clear();
        }
    }

    private void fillFeedbackArea(EvaluableReport report) {
        if (report.getProfessorFeedback() != null) {
            areaFeedback.setText(report.getProfessorFeedback());
        } else {
            areaFeedback.clear();
        }
    }

    private void showEvaluationContainer(boolean isVisible) {
        evaluationContainer.setVisible(isVisible);
        evaluationContainer.setManaged(isVisible);
    }

    @FXML
    private void handleViewPdfAction() {
        if (selectedReport == null || selectedReport.getSignedFileUrl() == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(selectedReport.getSignedFileUrl()));
        } catch (Exception e) {
            Controller.showAlert("Error de Archivo",
                    "No se pudo abrir la evidencia del alumno.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveEvaluationAction() {
        if (selectedReport == null) {
            return;
        }

        String rawGrade = fieldGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación antes de guardar.", AlertType.WARNING);
            return;
        }

        String feedback = areaFeedback.getText().trim();
        try {
            double grade = Double.parseDouble(rawGrade);
            saveEvaluation(selectedReport, grade, feedback);
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato inválido",
                    "La calificación debe ser un número decimal (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Datos inválidos", e.getMessage(), AlertType.WARNING);
        }
    }

    private void saveEvaluation(EvaluableReport report, double grade, String feedback) throws ManagerException {
        if (report.isProgressReport()) {
            ProgressReportType reportType = ProgressReportType.fromString(report.getReportKind());
            progressReportManager.evaluateProgressReport(report.getPractitionerId(), reportType, grade, feedback);
        } else {
            monthlyReportManager.evaluateReport(report.getReportId(), grade, feedback);
        }

        Controller.showAlert("Evaluación Guardada",
                "La evaluación del reporte fue registrada correctamente.", AlertType.INFORMATION);

        showEvaluationContainer(false);
        selectedReport = null;
        loadAllSubmittedReports();
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}