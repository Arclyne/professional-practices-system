package mx.uv.fei.presentation;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.ReportPdfGenerator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.manager.CloudStorageManager;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerReportsListController implements Initializable {

    private final MonthlyReportManager reportManager;
    private final ActivityManager activityManager;
    private final ReportPdfGenerator pdfGenerator;
    private final CloudStorageManager cloudStorageManager;
    private final AppStore store;

    @FXML private VBox reportsContainer;

    @FXML private VBox reportDetailsContainer;
    @FXML private Label detailTitle;
    @FXML private Label detailPeriod;
    @FXML private Label detailStatus;
    @FXML private Label detailGrade;
    @FXML private TextArea detailFeedback;
    @FXML private Button btnDownloadPdf;
    @FXML private Button btnUploadPdf;
    @FXML private Button btnViewSignedPdf;

    private MonthlyReport selectedReport = null;

    @Inject
    public PractitionerReportsListController(
            MonthlyReportManager reportManager,
            ActivityManager activityManager,
            ReportPdfGenerator pdfGenerator,
            CloudStorageManager cloudStorageManager,
            AppStore store
    ) {
        this.reportManager = reportManager;
        this.activityManager = activityManager;
        this.pdfGenerator = pdfGenerator;
        this.cloudStorageManager = cloudStorageManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportDetailsContainer.setVisible(false);
        loadReportsList();
    }

    private void loadReportsList() {
        reportsContainer.getChildren().clear();

        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentPractitioner != null ? currentPractitioner.getId() : 0;

            List<MonthlyReport> reports = reportManager.getPractitionerReports(practitionerId);

            if (reports.isEmpty()) {
                Label emptyLabel = new Label("Aún no tienes reportes generados. Crea tu primer reporte cuando termine el mes.");
                emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-font-style: italic;");
                reportsContainer.getChildren().add(emptyLabel);
            } else {
                for (MonthlyReport report : reports) {
                    reportsContainer.getChildren().add(createReportCard(report));
                }
            }
        } catch (ManagerException exception) {
            Controller.showAlert("Error de Carga", exception.getMessage(), AlertType.ERROR);
        }
    }

    private HBox createReportCard(MonthlyReport report) {
        HBox card = new HBox(15);
        String defaultStyle = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px; -fx-cursor: hand;";

        card.setStyle(defaultStyle);
        card.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);

        Label titleLabel = new Label("Reporte de " + report.getMonthName() + " " + report.getYear());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1f2937;");

        String statusColor = "#F59E0B";
        if ("Entregado".equalsIgnoreCase(report.getStatus())) statusColor = "#3B82F6";
        if ("Evaluado".equalsIgnoreCase(report.getStatus())) statusColor = "#10B981";

        Label statusLabel = new Label("Estado: " + report.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");

        infoBox.getChildren().addAll(titleLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        if (report.getGrade() != null) {
            Label gradeLabel = new Label(String.valueOf(report.getGrade()));
            gradeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #10B981;");
            rightBox.getChildren().add(gradeLabel);
        }

        card.getChildren().addAll(infoBox, spacer, rightBox);

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));
        card.setOnMouseClicked(e -> showReportDetails(report));

        return card;
    }

    private void showReportDetails(MonthlyReport report) {
        this.selectedReport = report;
        reportDetailsContainer.setVisible(true);

        detailTitle.setText("Reporte de " + report.getMonthName() + " " + report.getYear());
        detailPeriod.setText("Periodo: " + report.getStartDate() + " al " + report.getEndDate());

        String statusColor = "#F59E0B";
        if ("Entregado".equalsIgnoreCase(report.getStatus())) statusColor = "#3B82F6";
        if ("Evaluado".equalsIgnoreCase(report.getStatus())) statusColor = "#10B981";

        detailStatus.setText("Estado: " + report.getStatus());
        detailStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + statusColor + ";");

        if (report.getGrade() != null) {
            detailGrade.setText("Calificación: " + report.getGrade());
            detailGrade.setVisible(true);
            detailGrade.setManaged(true);
        } else {
            detailGrade.setVisible(false);
            detailGrade.setManaged(false);
        }

        if (report.getProfessorFeedback() != null && !report.getProfessorFeedback().trim().isEmpty()) {
            detailFeedback.setText(report.getProfessorFeedback());
        } else {
            detailFeedback.setText("El profesor aún no ha emitido comentarios para este reporte.");
        }

        btnDownloadPdf.setVisible(false); btnDownloadPdf.setManaged(false);
        btnUploadPdf.setVisible(false); btnUploadPdf.setManaged(false);
        btnViewSignedPdf.setVisible(false); btnViewSignedPdf.setManaged(false);

        if ("Pendiente de Firma".equalsIgnoreCase(report.getStatus())) {
            btnDownloadPdf.setVisible(true); btnDownloadPdf.setManaged(true);
            btnUploadPdf.setVisible(true); btnUploadPdf.setManaged(true);
        } else {
            btnViewSignedPdf.setVisible(true); btnViewSignedPdf.setManaged(true);
        }
    }

    @FXML
    private void handleDownloadPdfAction(ActionEvent event) {
        if (selectedReport == null) return;
        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            List<Activity> reportActivities = activityManager.getActivitiesByReport(selectedReport.getReportId());
            String pdfPath = pdfGenerator.generatePdf(selectedReport, currentPractitioner, reportActivities);

            File generatedFile = new File(pdfPath);
            if (generatedFile.exists()) {
                java.awt.Desktop.getDesktop().open(generatedFile);
                Controller.showAlert("PDF Generado", "El reporte se ha guardado en tu carpeta de Descargas y se abrirá a continuación.", AlertType.INFORMATION);
            }
        } catch (ManagerException exception) {
            Controller.showAlert("Error de Generación", exception.getMessage(), AlertType.ERROR);
        } catch (Exception e) {
            Controller.showAlert("Error de Sistema", "No se pudo abrir el visor de PDF.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUploadSignedPdfAction(ActionEvent event) {
        if (selectedReport == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Reporte Firmado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(reportsContainer.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String fileUrl = cloudStorageManager.uploadEvidenceFile(selectedFile);
                reportManager.submitSignedReport(selectedReport, fileUrl);

                Controller.showAlert("Reporte Enviado", "El documento firmado se subió exitosamente. Ahora el profesor podrá evaluarlo.", AlertType.INFORMATION);

                loadReportsList();
                showReportDetails(selectedReport);

            } catch (ManagerException exception) {
                Controller.showAlert("Error al Subir", exception.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleViewSignedPdfAction(ActionEvent event) {
        if (selectedReport != null && selectedReport.getSignedFileUrl() != null) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(selectedReport.getSignedFileUrl()));
            } catch (Exception e) {
                Controller.showAlert("Error de Enlace", "No se pudo abrir el archivo.", AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCreateNewReportAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_REPORT_GENERATOR));
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}