package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.ReportPdfGenerator;
import mx.uv.fei.domain.dto.CoveredReport;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.infrastructure.CloudStorageManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.reporting.MonthlyReportManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.sql.Date;
import java.util.List;

@Component
public class ProgressReportGeneratorController {

    private static final String REPORT_TYPE_FINAL = "Final";
    private static final String NO_FEEDBACK_MESSAGE = "El profesor aún no ha emitido comentarios.";
    private static final String NOT_GENERATED_MESSAGE =
            "Aún no generado. Genéralo, fírmalo y súbelo para revisión.";

    @FXML private Label intermediateStatusLabel;
    @FXML private Label intermediateGradeLabel;
    @FXML private TextArea intermediateFeedbackArea;
    @FXML private VBox intermediateDateContainer;
    @FXML private DatePicker periodEndDatePicker;
    @FXML private Button generateReportButton;
    @FXML private Button intermediateDownloadButton;
    @FXML private Button intermediateUploadButton;
    @FXML private Button intermediateViewButton;

    @FXML private Label finalLockedLabel;
    @FXML private Label finalStatusLabel;
    @FXML private Label finalGradeLabel;
    @FXML private TextArea finalFeedbackArea;
    @FXML private Button finalGenerateButton;
    @FXML private Button finalDownloadButton;
    @FXML private Button finalUploadButton;
    @FXML private Button finalViewButton;

    private final ProgressReportManager progressReportManager;
    private final MonthlyReportManager monthlyReportManager;
    private final CloudStorageManager cloudStorageManager;
    private final ReportPdfGenerator pdfGenerator;
    private final PeriodManager periodManager;
    private final AppStore store;

    private ProgressReport intermediateReport;
    private ProgressReport finalReport;
    private Period activePeriod;
    private int practitionerId;

    @Inject
    public ProgressReportGeneratorController(ProgressReportManager progressReportManager,
                                             MonthlyReportManager monthlyReportManager,
                                             CloudStorageManager cloudStorageManager, ReportPdfGenerator pdfGenerator,
                                             PeriodManager periodManager, AppStore store) {
        this.progressReportManager = progressReportManager;
        this.monthlyReportManager = monthlyReportManager;
        this.cloudStorageManager = cloudStorageManager;
        this.pdfGenerator = pdfGenerator;
        this.periodManager = periodManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        practitionerId = currentUser != null ? currentUser.getId() : 0;
        resolveActivePeriod();
        loadReports();
    }

    private void resolveActivePeriod() {
        try {
            activePeriod = periodManager.getActivePeriod();
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadReports() {
        intermediateReport = null;
        finalReport = null;
        try {
            List<ProgressReport> reports = progressReportManager.getProgressReportsByPractitioner(practitionerId);
            for (ProgressReport report : reports) {
                assignReport(report);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
        renderIntermediateCard();
        renderFinalCard();
    }

    private void assignReport(ProgressReport report) {
        if (REPORT_TYPE_FINAL.equals(report.getReportType())) {
            finalReport = report;
        } else {
            intermediateReport = report;
        }
    }

    private void renderIntermediateCard() {
        boolean notGenerated = intermediateReport == null;
        setNodeVisible(intermediateDateContainer, notGenerated);
        setNodeVisible(generateReportButton, notGenerated);
        generateReportButton.setDisable(notGenerated && activePeriod == null);
        applyReportState(intermediateReport, intermediateStatusLabel, intermediateGradeLabel,
                intermediateFeedbackArea, intermediateDownloadButton, intermediateUploadButton, intermediateViewButton);
    }

    private void renderFinalCard() {
        boolean intermediateApproved = intermediateReport != null
                && isStatus(intermediateReport, ReportStatus.EVALUATED);
        boolean notGenerated = finalReport == null;
        setNodeVisible(finalLockedLabel, !intermediateApproved);
        setNodeVisible(finalStatusLabel, intermediateApproved);
        setNodeVisible(finalGenerateButton, intermediateApproved && notGenerated);
        finalGenerateButton.setDisable(activePeriod == null);

        if (intermediateApproved) {
            applyReportState(finalReport, finalStatusLabel, finalGradeLabel, finalFeedbackArea,
                    finalDownloadButton, finalUploadButton, finalViewButton);
        } else {
            hideLockedFinalControls();
        }
    }

    private void hideLockedFinalControls() {
        setNodeVisible(finalGradeLabel, false);
        setNodeVisible(finalFeedbackArea, false);
        setNodeVisible(finalDownloadButton, false);
        setNodeVisible(finalUploadButton, false);
        setNodeVisible(finalViewButton, false);
    }

    private void applyReportState(ProgressReport report, Label statusLabel, Label gradeLabel, TextArea feedbackArea,
                                  Button downloadButton, Button uploadButton, Button viewButton) {
        boolean exists = report != null;
        boolean canResubmit = exists
                && (isStatus(report, ReportStatus.PENDING) || isStatus(report, ReportStatus.REJECTED));
        boolean submitted = exists && isStatus(report, ReportStatus.SUBMITTED);
        boolean evaluated = exists && isStatus(report, ReportStatus.EVALUATED);
        boolean rejected = exists && isStatus(report, ReportStatus.REJECTED);

        statusLabel.setText(buildStatusText(report));
        setNodeVisible(downloadButton, canResubmit);
        setNodeVisible(uploadButton, canResubmit);
        setNodeVisible(viewButton, submitted || evaluated);
        setNodeVisible(gradeLabel, evaluated);
        setNodeVisible(feedbackArea, evaluated || rejected);

        if (evaluated) {
            gradeLabel.setText("Calificación: " + report.getGrade() + " / 10.0");
        }
        if (evaluated || rejected) {
            feedbackArea.setText(feedbackOf(report));
        }
    }

    private String buildStatusText(ProgressReport report) {
        String statusText = NOT_GENERATED_MESSAGE;
        if (report != null) {
            statusText = "Estado: " + report.getStatus()
                    + "   |   Horas al generar: " + report.getTotalHoursAtSubmission();
        }
        return statusText;
    }

    private String feedbackOf(ProgressReport report) {
        String feedback = report.getProfessorFeedback();
        return feedback != null && !feedback.trim().isEmpty() ? feedback : NO_FEEDBACK_MESSAGE;
    }

    private boolean isStatus(ProgressReport report, ReportStatus status) {
        return status.getDatabaseValue().equalsIgnoreCase(report.getStatus());
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    @FXML
    private void handleGenerateReport() {
        if (activePeriod == null) {
            Controller.showAlert("Periodo no disponible",
                    "No hay un periodo académico activo para cubrir el reporte.", AlertType.WARNING);
        } else if (periodEndDatePicker.getValue() == null) {
            Controller.showAlert("Fecha requerida",
                    "Selecciona la fecha de corte del reporte intermedio.", AlertType.WARNING);
        } else {
            generateReport(ProgressReportType.INTERMEDIO, activePeriod.getStartDate(),
                    Date.valueOf(periodEndDatePicker.getValue()));
        }
    }

    @FXML
    private void handleGenerateFinalReport() {
        if (activePeriod == null) {
            Controller.showAlert("Periodo no disponible",
                    "No hay un periodo académico activo para cubrir el reporte.", AlertType.WARNING);
        } else {
            generateReport(ProgressReportType.FINAL, activePeriod.getStartDate(), activePeriod.getEndDate());
        }
    }

    private void generateReport(ProgressReportType reportType, Date periodStart, Date periodEnd) {
        try {
            progressReportManager.generateProgressReport(practitionerId, reportType, periodStart, periodEnd);
            Controller.showAlert("Reporte Generado",
                    "El reporte " + reportType.getDatabaseValue()
                            + " fue generado. Descárgalo, fírmalo y súbelo para su revisión.",
                    AlertType.INFORMATION);
            loadReports();
        } catch (ManagerException e) {
            Controller.showAlert("No se pudo generar", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleDownloadIntermediate() {
        downloadReport(intermediateReport);
    }

    @FXML
    private void handleDownloadFinal() {
        downloadReport(finalReport);
    }

    private void downloadReport(ProgressReport report) {
        if (report != null) {
            generateAndOpenPdf(report);
        }
    }

    private void generateAndOpenPdf(ProgressReport report) {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            List<CoveredReport> coveredReports = monthlyReportManager.getEvaluatedReportsWithActivitiesInRange(
                    practitionerId, report.getPeriodCoveredStart(), report.getPeriodCoveredEnd());
            String pdfPath = pdfGenerator.generateProgressReportPdf(report, currentUser, coveredReports);
            File generatedFile = new File(pdfPath);
            if (generatedFile.exists()) {
                Desktop.getDesktop().open(generatedFile);
                Controller.showAlert("PDF Generado", "El reporte se ha guardado y abierto.", AlertType.INFORMATION);
            }
        } catch (Exception e) {
            Controller.showAlert("Error de Sistema", "No se pudo generar o abrir el reporte.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUploadIntermediate() {
        uploadSignedReport(intermediateReport);
    }

    @FXML
    private void handleUploadFinal() {
        uploadSignedReport(finalReport);
    }

    private void uploadSignedReport(ProgressReport report) {
        if (report != null) {
            chooseAndUploadSignedReport(report);
        }
    }

    private void chooseAndUploadSignedReport(ProgressReport report) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Reporte Firmado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(intermediateStatusLabel.getScene().getWindow());

        if (selectedFile != null) {
            submitSignedReport(report, selectedFile);
        }
    }

    private void submitSignedReport(ProgressReport report, File selectedFile) {
        try {
            String fileUrl = cloudStorageManager.uploadEvidenceFile(selectedFile);
            ProgressReportType reportType = ProgressReportType.fromString(report.getReportType());
            progressReportManager.submitSignedProgressReport(practitionerId, reportType, fileUrl);
            Controller.showAlert("Reporte Enviado",
                    "El reporte firmado fue subido correctamente.", AlertType.INFORMATION);
            loadReports();
        } catch (ManagerException e) {
            Controller.showAlert("Error al Subir", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewIntermediate() {
        viewSignedReport(intermediateReport);
    }

    @FXML
    private void handleViewFinal() {
        viewSignedReport(finalReport);
    }

    private void viewSignedReport(ProgressReport report) {
        boolean hasSignedFile = report != null && report.getSignedFileUrl() != null;
        if (hasSignedFile) {
            openSignedReport(report);
        }
    }

    private void openSignedReport(ProgressReport report) {
        try {
            Desktop.getDesktop().browse(new URI(report.getSignedFileUrl()));
        } catch (Exception e) {
            Controller.showAlert("Error de Archivo", "No se pudo abrir el archivo firmado.", AlertType.ERROR);
        }
    }
}
