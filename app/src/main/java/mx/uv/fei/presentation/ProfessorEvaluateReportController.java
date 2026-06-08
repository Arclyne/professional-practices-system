package mx.uv.fei.presentation;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.EvaluableReport;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;


@Component
public class ProfessorEvaluateReportController implements Initializable {

    private static final String MSG_GRADE_REQUIRED   = "Campo requerido";
    private static final String MSG_ENTER_GRADE      = "Ingresa la calificación antes de guardar.";
    private static final String MSG_INVALID_FORMAT   = "Formato inválido";
    private static final String MSG_NUMBER_FORMAT    = "La calificación debe ser un número decimal (ej. 9.5).";
    private static final String MSG_LOAD_ERROR       = "Error de Carga";
    private static final String MSG_PDF_ERROR        = "No se pudo abrir la evidencia del alumno.";
    private static final String LABEL_EVALUATED      = "✓ ";
    private static final String LABEL_PENDING        = "⏳ ";

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
    private final AppStore store;

    private EvaluableReport selectedReport;

    @Inject
    public ProfessorEvaluateReportController(
            MonthlyReportManager monthlyReportManager,
            ProgressReportManager progressReportManager,
            AppStore store) {
        this.monthlyReportManager = monthlyReportManager;
        this.progressReportManager = progressReportManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationContainer.setVisible(false);
        evaluationContainer.setManaged(false);
        configureListView();
        loadAllSubmittedReports();
    }

    private void configureListView() {
        reportsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(EvaluableReport report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) {
                    setText(null);
                } else {
                    boolean isEvaluated = ReportStatus.EVALUATED.getDatabaseValue()
                            .equals(report.getStatus());
                    String prefix = isEvaluated ? LABEL_EVALUATED : LABEL_PENDING;
                    setText(prefix + report.getDisplayName());
                }
            }
        });

        reportsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        showReportDetails(newValue);
                    }
                }
        );
    }

    private void loadAllSubmittedReports() {
        List<EvaluableReport> allReports = new ArrayList<>();

        loadMonthlyReports(allReports);
        loadProgressReports(allReports);

        reportsListView.setItems(FXCollections.observableArrayList(allReports));
    }

    private void loadMonthlyReports(List<EvaluableReport> target) {
        try {
            List<MonthlyReport> monthly = monthlyReportManager.getReportsForEvaluation();
            for (MonthlyReport report : monthly) {
                target.add(EvaluableReport.fromMonthlyReport(report));
            }
        } catch (ManagerException exception) {
            Controller.showAlert(MSG_LOAD_ERROR,
                    "Reportes mensuales: " + exception.getMessage(), AlertType.WARNING);
        }
    }

    private void loadProgressReports(List<EvaluableReport> target) {
        try {
            List<ProgressReport> progress = progressReportManager.getSubmittedProgressReports();
            for (ProgressReport report : progress) {
                target.add(EvaluableReport.fromProgressReport(report));
            }
        } catch (ManagerException exception) {
            Controller.showAlert(MSG_LOAD_ERROR,
                    "Reportes de avance: " + exception.getMessage(), AlertType.WARNING);
        }
    }

    private void showReportDetails(EvaluableReport report) {
        selectedReport = report;

        evaluationContainer.setVisible(true);
        evaluationContainer.setManaged(true);

        labelReportKind.setText("Tipo de reporte: " + report.getReportKind());
        labelReportInfo.setText(report.getDisplayName()
                + "\nEstado: " + report.getStatus());

        boolean hasPdf = report.getSignedFileUrl() != null
                && !report.getSignedFileUrl().isEmpty();
        btnViewPdf.setDisable(!hasPdf);

        if (report.getGrade() != null) {
            fieldGrade.setText(String.valueOf(report.getGrade()));
        } else {
            fieldGrade.clear();
        }

        if (report.getProfessorFeedback() != null) {
            areaFeedback.setText(report.getProfessorFeedback());
        } else {
            areaFeedback.clear();
        }
    }

    @FXML
    private void handleViewPdfAction(ActionEvent event) {
        if (selectedReport == null || selectedReport.getSignedFileUrl() == null) {
            return;
        }

        try {
            java.awt.Desktop.getDesktop().browse(
                    new java.net.URI(selectedReport.getSignedFileUrl()));
        } catch (Exception exception) {
            Controller.showAlert("Error de Archivo", MSG_PDF_ERROR, AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveEvaluationAction(ActionEvent event) {
        if (selectedReport == null) {
            return;
        }

        String rawGrade = fieldGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert(MSG_GRADE_REQUIRED, MSG_ENTER_GRADE, AlertType.WARNING);
            return;
        }

        String feedback = areaFeedback.getText().trim();

        try {
            double grade = Double.parseDouble(rawGrade);
            saveEvaluation(selectedReport, grade, feedback);
        } catch (NumberFormatException exception) {
            Controller.showAlert(MSG_INVALID_FORMAT, MSG_NUMBER_FORMAT, AlertType.WARNING);
        } catch (ManagerException exception) {
            Controller.showAlert("Datos inválidos", exception.getMessage(), AlertType.WARNING);
        }
    }

    private void saveEvaluation(EvaluableReport report, double grade, String feedback)
            throws ManagerException {
        if (report.isProgressReport()) {
            ProgressReportType reportType = ProgressReportType.fromString(report.getReportKind());
            progressReportManager.evaluateProgressReport(
                    report.getPractitionerId(), reportType, grade, feedback);
        } else {
            monthlyReportManager.evaluateReport(report.getReportId(), grade, feedback);
        }

        Controller.showAlert("Evaluación Guardada",
                "La evaluación del reporte fue registrada correctamente.",
                AlertType.INFORMATION);

        evaluationContainer.setVisible(false);
        evaluationContainer.setManaged(false);
        selectedReport = null;
        loadAllSubmittedReports();
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}