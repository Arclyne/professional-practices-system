package mx.uv.fei.presentation.practitioner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.ReportPdfGenerator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.ActivityManager;
import mx.uv.fei.domain.manager.infrastructure.CloudStorageManager;
import mx.uv.fei.domain.manager.reporting.MonthlyReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerReportsListController implements Initializable {

    private static final String MSG_PDF_OPEN_ERROR  = "No se pudo abrir el visor de PDF.";
    private static final String MSG_LINK_OPEN_ERROR = "No se pudo abrir el archivo.";
    private static final String STATUS_FILTER_ALL = "Todos";
    private static final String STATUS_FILTER_PENDING_REVIEW = "Pendientes de revisión";
    private static final String STATUS_FILTER_REVIEWED = "Revisadas";
    private static final String MONTH_FILTER_ALL = "Todos los meses";

    private final MonthlyReportManager reportManager;
    private final ActivityManager activityManager;
    private final ReportPdfGenerator pdfGenerator;
    private final CloudStorageManager cloudStorageManager;
    private final AppStore store;

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> monthFilterComboBox;
    @FXML private ListView<MonthlyReport> reportsListView;
    @FXML private VBox reportDetailsContainer;
    @FXML private Label detailTitle;
    @FXML private Label detailPeriod;
    @FXML private Label detailStatus;
    @FXML private Label detailGrade;
    @FXML private TextArea detailFeedback;
    @FXML private Button btnDownloadPdf;
    @FXML private Button btnUploadPdf;
    @FXML private Button btnViewSignedPdf;
    @FXML private Button btnCreateNewReport;
    @FXML private Label labelNoProject;

    private final List<MonthlyReport> allReports = new ArrayList<>();
    private MonthlyReport selectedReport;

    @Inject
    public PractitionerReportsListController(
            MonthlyReportManager reportManager,
            ActivityManager activityManager,
            ReportPdfGenerator pdfGenerator,
            CloudStorageManager cloudStorageManager,
            AppStore store) {
        this.reportManager = reportManager;
        this.activityManager = activityManager;
        this.pdfGenerator = pdfGenerator;
        this.cloudStorageManager = cloudStorageManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportDetailsContainer.setVisible(false);
        labelNoProject.setVisible(false);
        labelNoProject.setManaged(false);

        User currentUser = store.getState().sessionState().currentUserInSession();
        int practitionerId = currentUser != null ? currentUser.getId() : 0;

        boolean canRegisterReports = checkAndEnforceReportsAccess(practitionerId);

        if (canRegisterReports) {
            configureListView();
            configureFilters();
            loadReportsList(practitionerId);
        }
    }

    private void configureFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                STATUS_FILTER_ALL, STATUS_FILTER_PENDING_REVIEW, STATUS_FILTER_REVIEWED));
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> applyFilters());
        monthFilterComboBox.valueProperty().addListener((_, _, _) -> applyFilters());
    }

    private boolean checkAndEnforceReportsAccess(int practitionerId) {
        String blockingMessage = resolveBlockingMessage(practitionerId);

        if (blockingMessage != null) {
            labelNoProject.setText(blockingMessage);
            labelNoProject.setVisible(true);
            labelNoProject.setManaged(true);
            btnCreateNewReport.setDisable(true);
        }

        return blockingMessage == null;
    }

    private String resolveBlockingMessage(int practitionerId) {
        String blockingMessage = null;

        try {
            if (!reportManager.verifyHasAssignedProject(practitionerId)) {
                blockingMessage = "Necesitas un proyecto asignado para registrar reportes.";
            } else if (!reportManager.verifyAllInitialDocumentsAccepted(practitionerId)) {
                blockingMessage = "Aún no puedes registrar reportes. "
                        + "Tu profesor debe aceptar todos tus documentos iniciales primero.";
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de verificación", e.getMessage(), AlertType.ERROR);
            blockingMessage = "No se pudo verificar tu acceso a los reportes.";
        }

        return blockingMessage;
    }

    private void configureListView() {
        reportsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MonthlyReport report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) {
                    setText(null);
                } else {
                    setText("Reporte de " + report.getMonthName() + " " + report.getYear()
                            + "\nEstado: " + report.getStatus());
                }
            }
        });

        reportsListView.getSelectionModel().selectedIndexProperty().addListener(
                (_, _, selectedIndex) -> showSelectedReportDetails(selectedIndex.intValue()));
    }

    private void showSelectedReportDetails(int selectedIndex) {
        if (selectedIndex >= 0 && selectedIndex < reportsListView.getItems().size()) {
            showReportDetails(reportsListView.getItems().get(selectedIndex));
        }
    }

    private void loadReportsList(int practitionerId) {
        try {
            allReports.clear();
            allReports.addAll(reportManager.getPractitionerReports(practitionerId));
            populateMonthFilter();
            applyFilters();
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void populateMonthFilter() {
        Set<String> monthOptions = new LinkedHashSet<>();
        monthOptions.add(MONTH_FILTER_ALL);
        for (MonthlyReport report : allReports) {
            monthOptions.add(monthLabel(report));
        }
        String previousSelection = monthFilterComboBox.getValue();
        monthFilterComboBox.setItems(FXCollections.observableArrayList(monthOptions));
        monthFilterComboBox.setValue(monthOptions.contains(previousSelection) ? previousSelection : MONTH_FILTER_ALL);
    }

    private void applyFilters() {
        List<MonthlyReport> visibleReports = new ArrayList<>();
        for (MonthlyReport report : allReports) {
            if (matchesStatusFilter(report) && matchesMonthFilter(report)) {
                visibleReports.add(report);
            }
        }
        reportsListView.setItems(FXCollections.observableArrayList(visibleReports));
    }

    private boolean matchesStatusFilter(MonthlyReport report) {
        String selectedStatus = statusFilterComboBox.getValue();
        boolean isMatch = selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus);

        if (!isMatch && STATUS_FILTER_PENDING_REVIEW.equals(selectedStatus)) {
            isMatch = ReportStatus.SUBMITTED.getDatabaseValue().equals(report.getStatus());
        } else if (!isMatch && STATUS_FILTER_REVIEWED.equals(selectedStatus)) {
            isMatch = ReportStatus.EVALUATED.getDatabaseValue().equals(report.getStatus());
        }

        return isMatch;
    }

    private boolean matchesMonthFilter(MonthlyReport report) {
        String selectedMonth = monthFilterComboBox.getValue();
        return selectedMonth == null || MONTH_FILTER_ALL.equals(selectedMonth) || selectedMonth.equals(monthLabel(report));
    }

    private String monthLabel(MonthlyReport report) {
        return report.getMonthName() + " " + report.getYear();
    }

    private void showReportDetails(MonthlyReport report) {
        selectedReport = report;
        reportDetailsContainer.setVisible(true);

        detailTitle.setText("Reporte de " + report.getMonthName() + " " + report.getYear());
        detailPeriod.setText("Periodo: " + report.getStartDate() + " al " + report.getEndDate());
        detailStatus.setText("Estado: " + report.getStatus());

        boolean hasGrade = report.getGrade() != null;
        detailGrade.setVisible(hasGrade);
        detailGrade.setManaged(hasGrade);
        if (hasGrade) {
            detailGrade.setText("Calificación: " + report.getGrade());
        }

        boolean hasFeedback = report.getProfessorFeedback() != null
                && !report.getProfessorFeedback().trim().isEmpty();
        detailFeedback.setText(hasFeedback
                ? report.getProfessorFeedback()
                : "El profesor aún no ha emitido comentarios para este reporte.");

        boolean isPending = ReportStatus.PENDING.getDatabaseValue()
                .equalsIgnoreCase(report.getStatus());

        btnDownloadPdf.setVisible(isPending);
        btnDownloadPdf.setManaged(isPending);
        btnUploadPdf.setVisible(isPending);
        btnUploadPdf.setManaged(isPending);
        btnViewSignedPdf.setVisible(!isPending);
        btnViewSignedPdf.setManaged(!isPending);
    }

    @FXML
    private void handleDownloadPdfAction(ActionEvent event) {
        if (selectedReport != null) {
            downloadSelectedReportPdf();
        }
    }

    private void downloadSelectedReportPdf() {
        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            List<Activity> reportActivities = activityManager.getActivitiesByReport(selectedReport.getReportId());
            String pdfPath = pdfGenerator.generateMonthlyReportPdf(selectedReport, currentPractitioner, reportActivities);

            File generatedFile = new File(pdfPath);
            if (generatedFile.exists()) {
                java.awt.Desktop.getDesktop().open(generatedFile);
                Controller.showAlert("PDF Generado", "El reporte se ha guardado en Descargas.", AlertType.INFORMATION);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Generación", e.getMessage(), AlertType.ERROR);
        } catch (Exception exception) {
            Controller.showAlert("Error de Sistema", MSG_PDF_OPEN_ERROR, AlertType.ERROR);
        }
    }

    @FXML
    private void handleUploadSignedPdfAction(ActionEvent event) {
        if (selectedReport != null) {
            chooseAndUploadSignedReport();
        }
    }

    private void chooseAndUploadSignedReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Reporte Firmado");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(reportsListView.getScene().getWindow());

        if (selectedFile != null) {
            uploadSignedReport(selectedFile);
        }
    }

    private void uploadSignedReport(File selectedFile) {
        try {
            String fileUrl = cloudStorageManager.uploadEvidenceFile(selectedFile);
            reportManager.submitSignedReport(selectedReport, fileUrl);

            Controller.showAlert("Reporte Enviado", "Documento subido exitosamente.", AlertType.INFORMATION);

            User currentUser = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentUser != null ? currentUser.getId() : 0;
            loadReportsList(practitionerId);
            showReportDetails(selectedReport);
        } catch (ManagerException e) {
            Controller.showAlert("Error al Subir", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewSignedPdfAction(ActionEvent event) {
        boolean hasSignedFile = selectedReport != null && selectedReport.getSignedFileUrl() != null;
        if (hasSignedFile) {
            openSignedReport();
        }
    }

    private void openSignedReport() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(selectedReport.getSignedFileUrl()));
        } catch (Exception exception) {
            Controller.showAlert("Error de Enlace", MSG_LINK_OPEN_ERROR, AlertType.ERROR);
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
