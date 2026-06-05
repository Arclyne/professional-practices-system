package mx.uv.fei.presentation;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class ProfessorEvaluateReportController implements Initializable {

    private final MonthlyReportManager reportManager;
    private final AppStore store;

    @FXML private ListView<MonthlyReport> reportsListView;
    @FXML private VBox evaluationContainer;

    @FXML private Label labelReportInfo;
    @FXML private Button btnViewPdf;
    @FXML private TextField fieldGrade;
    @FXML private TextArea areaFeedback;
    @FXML private Button btnSaveEvaluation;

    private MonthlyReport selectedReport = null;

    @Inject
    public ProfessorEvaluateReportController(MonthlyReportManager reportManager, AppStore store) {
        this.reportManager = reportManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evaluationContainer.setVisible(false);
        configureListView();
        loadSubmittedReports();
    }

    private void configureListView() {
        reportsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MonthlyReport report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) {
                    setText(null);
                } else {
                    String statusMark = "Evaluado".equals(report.getStatus()) ? "✅ " : "⏳ ";
                    setText(statusMark + report.getMonthName() + " " + report.getYear() + " - Practicante ID: " + report.getPractitionerId());
                }
            }
        });
        reportsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showReportDetails(newSelection);
            }
        });
    }

    private void loadSubmittedReports() {
        try {
            List<MonthlyReport> reports = reportManager.getReportsForEvaluation();
            ObservableList<MonthlyReport> observableReports = FXCollections.observableArrayList(reports);
            reportsListView.setItems(observableReports);
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void showReportDetails(MonthlyReport report) {
        this.selectedReport = report;
        evaluationContainer.setVisible(true);

        labelReportInfo.setText("Reporte: " + report.getMonthName() + " " + report.getYear() + "\n" +
                "Fechas: " + report.getStartDate() + " al " + report.getEndDate() + "\n" +
                "Estado: " + report.getStatus());

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

        btnViewPdf.setDisable(report.getSignedFileUrl() == null || report.getSignedFileUrl().isEmpty());
    }

    @FXML
    private void handleViewPdfAction(ActionEvent event) {
        if (selectedReport != null && selectedReport.getSignedFileUrl() != null) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(selectedReport.getSignedFileUrl()));
            } catch (Exception e) {
                Controller.showAlert("Error de Archivo", "No se pudo abrir la evidencia del alumno.", AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleSaveEvaluationAction(ActionEvent event) {
        if (selectedReport == null) return;

        try {
            Double grade = Double.parseDouble(fieldGrade.getText().trim());
            String feedback = areaFeedback.getText().trim();

            reportManager.evaluateReport(selectedReport.getReportId(), grade, feedback);

            Controller.showAlert("Evaluación Exitosa", "La calificación y retroalimentación han sido guardadas.", AlertType.INFORMATION);

            loadSubmittedReports();
            evaluationContainer.setVisible(false);

        } catch (NumberFormatException e) {
            Controller.showAlert("Formato Inválido", "La calificación debe ser un número válido (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error al Evaluar", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}