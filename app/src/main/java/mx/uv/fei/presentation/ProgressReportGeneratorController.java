package mx.uv.fei.presentation;

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

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.sql.Date;
import java.util.List;

@Component
public class ProgressReportGeneratorController {

    private static final String TYPE_INTERMEDIATE_LABEL = "Intermedio (mín. 210 horas)";
    private static final String TYPE_FINAL_LABEL = "Final (mín. 420 horas)";
    private static final String STATUS_PENDING_SIGNATURE = "Pendiente de Firma";

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
    public ProgressReportGeneratorController(ProgressReportManager progressReportManager,
                                             CloudStorageManager cloudStorageManager, ReportPdfGenerator pdfGenerator, AppStore store) {
        this.progressReportManager = progressReportManager;
        this.cloudStorageManager = cloudStorageManager;
        this.pdfGenerator = pdfGenerator;
        this.store = store;
    }

    @FXML
    public void initialize() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        practitionerId = currentUser != null ? currentUser.getId() : 0;

        reportTypeComboBox.getItems().addAll(TYPE_INTERMEDIATE_LABEL, TYPE_FINAL_LABEL);
        reportTypeComboBox.getSelectionModel().selectFirst();
        showExistingReportContainer(false);
        loadExistingReports();
    }

    private void loadExistingReports() {
        try {
            List<ProgressReport> existingReports = progressReportManager
                    .getProgressReportsByPractitioner(practitionerId);
            if (!existingReports.isEmpty()) {
                showExistingReport(existingReports.getFirst());
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void showExistingReport(ProgressReport report) {
        currentProgressReport = report;
        showExistingReportContainer(true);

        existingReportStatusLabel.setText("Tipo: " + report.getReportType()
                + "   |   Estado: " + report.getStatus());
        existingReportHoursLabel.setText("Horas al generar: " + report.getTotalHoursAtSubmission());

        boolean isPendingSignature = STATUS_PENDING_SIGNATURE.equalsIgnoreCase(report.getStatus());
        uploadSignedButton.setVisible(isPendingSignature);
        uploadSignedButton.setManaged(isPendingSignature);
        viewSignedButton.setVisible(!isPendingSignature);
        viewSignedButton.setManaged(!isPendingSignature);
    }

    private void showExistingReportContainer(boolean isVisible) {
        existingReportContainer.setVisible(isVisible);
        existingReportContainer.setManaged(isVisible);
    }

    @FXML
    private void handleGenerateReport() {
        if (periodStartDatePicker.getValue() == null || periodEndDatePicker.getValue() == null) {
            Controller.showAlert("Fechas requeridas",
                    "Selecciona las fechas de inicio y fin del periodo cubierto.", AlertType.WARNING);
            return;
        }

        ProgressReportType reportType = resolveSelectedType();
        Date periodStart = Date.valueOf(periodStartDatePicker.getValue());
        Date periodEnd = Date.valueOf(periodEndDatePicker.getValue());

        try {
            ProgressReport generatedReport = progressReportManager.generateProgressReport(
                    practitionerId, reportType, periodStart, periodEnd);
            Controller.showAlert("Reporte Generado",
                    "El reporte " + reportType.getDatabaseValue() + " fue generado.\n"
                            + "Horas acumuladas: " + generatedReport.getTotalHoursAtSubmission() + "\n"
                            + "Descárgalo, fírmalo y súbelo para su revisión.",
                    AlertType.INFORMATION);
            showExistingReport(generatedReport);
        } catch (ManagerException e) {
            Controller.showAlert("No se pudo generar", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleDownloadReport() {
        if (currentProgressReport == null) {
            return;
        }
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            String pdfPath = pdfGenerator.generateProgressReportPdf(currentProgressReport, currentUser);
            File generatedFile = new File(pdfPath);
            if (generatedFile.exists()) {
                Desktop.getDesktop().open(generatedFile);
                Controller.showAlert("PDF Generado",
                        "El reporte se ha guardado y abierto.", AlertType.INFORMATION);
            }
        } catch (Exception e) {
            Controller.showAlert("Error de Sistema",
                    "No se pudo generar o abrir el reporte.", AlertType.ERROR);
        }
    }

    private ProgressReportType resolveSelectedType() {
        return TYPE_FINAL_LABEL.equals(reportTypeComboBox.getValue())
                ? ProgressReportType.FINAL
                : ProgressReportType.INTERMEDIO;
    }

    @FXML
    private void handleUploadSignedReport() {
        if (currentProgressReport == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Reporte Firmado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(uploadSignedButton.getScene().getWindow());

        if (selectedFile != null) {
            uploadSignedReport(selectedFile);
        }
    }

    private void uploadSignedReport(File selectedFile) {
        try {
            String fileUrl = cloudStorageManager.uploadEvidenceFile(selectedFile);
            ProgressReportType reportType = ProgressReportType.fromString(currentProgressReport.getReportType());
            progressReportManager.submitSignedProgressReport(practitionerId, reportType, fileUrl);
            Controller.showAlert("Reporte Enviado",
                    "El reporte firmado fue subido correctamente.", AlertType.INFORMATION);
            loadExistingReports();
        } catch (ManagerException e) {
            Controller.showAlert("Error al Subir", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewSignedReport() {
        if (currentProgressReport == null || currentProgressReport.getSignedFileUrl() == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(currentProgressReport.getSignedFileUrl()));
        } catch (Exception e) {
            Controller.showAlert("Error de Archivo",
                    "No se pudo abrir el archivo firmado.", AlertType.ERROR);
        }
    }
}