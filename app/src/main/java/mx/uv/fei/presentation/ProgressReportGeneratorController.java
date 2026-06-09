package mx.uv.fei.presentation;

import java.io.File;
import java.sql.Date;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.ReportPdfGenerator;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CloudStorageManager;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class ProgressReportGeneratorController {

    private static final String TYPE_INTERMEDIO = "Intermedio (mín. 210 horas)";
    private static final String TYPE_FINAL      = "Final (mín. 420 horas)";

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker periodStartDatePicker;
    @FXML private DatePicker periodEndDatePicker;
    @FXML private Label hoursInfoLabel;
    @FXML private Button generateReportButton;
    @FXML private Button downloadReportButton;

    @FXML private VBox existingReportContainer;
    @FXML private Label existingReportStatusLabel;
    @FXML private Label existingReportHoursLabel;
    @FXML private Button uploadSignedButton;
    @FXML private Button viewSignedButton;

    private final ProgressReportManager progressReportManager;
    private final CloudStorageManager cloudStorageManager;
    private final ReportPdfGenerator pdfGenerator;
    private final AppStore store;

    private ProgressReport currentProgressReport;
    private int practitionerId;

    @Inject
    public ProgressReportGeneratorController(
            ProgressReportManager progressReportManager,
            CloudStorageManager cloudStorageManager,
            ReportPdfGenerator pdfGenerator,
            AppStore store) {
        this.progressReportManager = progressReportManager;
        this.cloudStorageManager = cloudStorageManager;
        this.pdfGenerator = pdfGenerator;
        this.store = store;
    }

    @FXML
    public void initialize() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        practitionerId = currentUser != null ? currentUser.getId() : 0;

        reportTypeComboBox.getItems().addAll(TYPE_INTERMEDIO, TYPE_FINAL);
        reportTypeComboBox.getSelectionModel().selectFirst();

        existingReportContainer.setVisible(false);
        existingReportContainer.setManaged(false);

        loadExistingReports();
    }

    private void loadExistingReports() {
        try {
            List<ProgressReport> existing = progressReportManager.getProgressReportsByPractitioner(practitionerId);
            if (!existing.isEmpty()) {
                showExistingReport(existing.getFirst());
            }
        } catch (ManagerException exception) {
            Controller.showAlert("Error de Carga", exception.getMessage(), AlertType.ERROR);
        }
    }

    private void showExistingReport(ProgressReport report) {
        currentProgressReport = report;
        existingReportContainer.setVisible(true);
        existingReportContainer.setManaged(true);

        existingReportStatusLabel.setText("Tipo: " + report.getReportType()
                + "   |   Estado: " + report.getStatus());
        existingReportHoursLabel.setText("Horas al generar: " + report.getTotalHoursAtSubmission());

        boolean isPendingSignature = "Pendiente de Firma".equalsIgnoreCase(report.getStatus());
        uploadSignedButton.setVisible(isPendingSignature);
        uploadSignedButton.setManaged(isPendingSignature);
        viewSignedButton.setVisible(!isPendingSignature);
        viewSignedButton.setManaged(!isPendingSignature);
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        if (periodStartDatePicker.getValue() == null || periodEndDatePicker.getValue() == null) {
            Controller.showAlert("Fechas requeridas",
                    "Selecciona las fechas de inicio y fin del periodo cubierto.", AlertType.WARNING);
            return;
        }

        ProgressReportType reportType = resolveSelectedType();
        Date periodStart = Date.valueOf(periodStartDatePicker.getValue());
        Date periodEnd   = Date.valueOf(periodEndDatePicker.getValue());

        try {
            ProgressReport generated = progressReportManager.generateProgressReport(
                    practitionerId, reportType, periodStart, periodEnd);

            Controller.showAlert("Reporte Generado",
                    "El reporte " + reportType.getDatabaseValue() + " fue generado.\n" +
                            "Horas acumuladas: " + generated.getTotalHoursAtSubmission() + "\n" +
                            "Descárgalo, fírmalo y súbelo para su revisión.",
                    AlertType.INFORMATION);

            showExistingReport(generated);
        } catch (ManagerException exception) {
            Controller.showAlert("No se pudo generar", exception.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleDownloadReport(ActionEvent event) {
        if (currentProgressReport != null) {
            try {
                User currentPractitioner = store.getState().sessionState().currentUserInSession();

                String pdfPath = pdfGenerator.generateProgressReportPdf(currentProgressReport, currentPractitioner);

                File generatedFile = new File(pdfPath);
                if (generatedFile.exists()) {
                    java.awt.Desktop.getDesktop().open(generatedFile);
                    Controller.showAlert("PDF Generado", "El reporte se ha guardado y abierto.", AlertType.INFORMATION);
                }
            } catch (Exception e) {
                Controller.showAlert("Error de Sistema", "No se pudo generar o abrir el reporte: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private ProgressReportType resolveSelectedType() {
        ProgressReportType resolvedType = ProgressReportType.INTERMEDIO;

        if (TYPE_FINAL.equals(reportTypeComboBox.getValue())) {
            resolvedType = ProgressReportType.FINAL;
        }

        return resolvedType;
    }

    @FXML
    private void handleUploadSignedReport(ActionEvent event) {
        if (currentProgressReport == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Reporte Firmado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(uploadSignedButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String fileUrl = cloudStorageManager.uploadEvidenceFile(selectedFile);
                ProgressReportType reportType = ProgressReportType.fromString(currentProgressReport.getReportType());
                progressReportManager.submitSignedProgressReport(practitionerId, reportType, fileUrl);

                Controller.showAlert("Reporte Enviado", "El reporte firmado fue subido correctamente.", AlertType.INFORMATION);
                loadExistingReports();
            } catch (ManagerException exception) {
                Controller.showAlert("Error al Subir", exception.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleViewSignedReport(ActionEvent event) {
        if (currentProgressReport != null && currentProgressReport.getSignedFileUrl() != null) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(currentProgressReport.getSignedFileUrl()));
            } catch (Exception exception) {
                Controller.showAlert("Error de Archivo", "No se pudo abrir el archivo firmado.", AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}