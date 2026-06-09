package mx.uv.fei.presentation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.SelfEvaluationPdfGenerator;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.manager.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerSelfEvaluationController {

    private static final String REPORT_TYPE_FINAL  = "Final";
    private static final String STATUS_REVIEWED    = "Revisada";
    private static final String EVIDENCE_PENDING   = "pendiente";
    private static final String PDF_INITIAL_NAME   = "PRAIS-03_Autoevaluacion.pdf";
    private static final String MSG_SAVE_FIRST     = "Primero debe Guardar su evaluación.";
    private static final String MSG_RESTRICTED     = "Acceso Restringido";
    private static final String MSG_ALREADY_SAVED  = "La autoevaluación ya estaba registrada.";
    private static final String MSG_SAVE_SUCCESS   = "Autoevaluación guardada correctamente. Ahora puede descargar el PDF para firmarlo.";

    @FXML private Label statusLabel;
    @FXML private ComboBox<Integer> q1ComboBox;
    @FXML private ComboBox<Integer> q2ComboBox;
    @FXML private ComboBox<Integer> q3ComboBox;
    @FXML private ComboBox<Integer> q4ComboBox;
    @FXML private ComboBox<Integer> q5ComboBox;
    @FXML private ComboBox<Integer> q6ComboBox;
    @FXML private ComboBox<Integer> q7ComboBox;
    @FXML private ComboBox<Integer> q8ComboBox;
    @FXML private ComboBox<Integer> q9ComboBox;
    @FXML private ComboBox<Integer> q10ComboBox;
    @FXML private Button saveButton;
    @FXML private Button downloadPdfButton;
    @FXML private Button uploadSignedButton;

    private final SelfEvaluationManager selfEvaluationManager;
    private final ProgressReportManager progressReportManager;
    private final AppStore appStore;

    private SelfEvaluation currentEvaluation;
    private ProgressReport finalReport;

    @Inject
    public PractitionerSelfEvaluationController(
            SelfEvaluationManager selfEvaluationManager,
            ProgressReportManager progressReportManager,
            AppStore appStore) {
        this.selfEvaluationManager = selfEvaluationManager;
        this.progressReportManager = progressReportManager;
        this.appStore = appStore;
    }

    @FXML
    public void initialize() {
        ObservableList<Integer> scoreOptions = FXCollections.observableArrayList(1, 2, 3, 4, 5);

        initializeComboBox(q1ComboBox, scoreOptions);
        initializeComboBox(q2ComboBox, scoreOptions);
        initializeComboBox(q3ComboBox, scoreOptions);
        initializeComboBox(q4ComboBox, scoreOptions);
        initializeComboBox(q5ComboBox, scoreOptions);
        initializeComboBox(q6ComboBox, scoreOptions);
        initializeComboBox(q7ComboBox, scoreOptions);
        initializeComboBox(q8ComboBox, scoreOptions);
        initializeComboBox(q9ComboBox, scoreOptions);
        initializeComboBox(q10ComboBox, scoreOptions);

        loadEvaluationState();
    }

    private void initializeComboBox(ComboBox<Integer> comboBox, ObservableList<Integer> options) {
        comboBox.setItems(options);
        comboBox.setValue(1);
    }

    private void loadEvaluationState() {
        try {
            User currentUser = appStore.getState().sessionState().currentUserInSession();
            List<ProgressReport> reports = progressReportManager
                    .getProgressReportsByPractitioner(currentUser.getId());

            finalReport = reports.stream()
                    .filter(r -> REPORT_TYPE_FINAL.equals(r.getReportType()))
                    .findFirst()
                    .orElse(null);

            applyStateForFinalReport();
        } catch (ManagerException e) {
            disableAllControls("Ocurrió un error al cargar la información.");
            Controller.showAlert("Error", "No se pudo verificar el estado de la autoevaluación: "
                    + e.getMessage(), AlertType.ERROR);
        }
    }

    private void applyStateForFinalReport() throws ManagerException {
        if (finalReport == null) {
            statusLabel.setText("Estado: Reporte Final no generado");
            disableAllControls("Debes generar tu Reporte Final antes de realizar la autoevaluación.");
        } else {
            applyStateForReportStatus();
        }
    }

    private void applyStateForReportStatus() throws ManagerException {
        String reportStatus = finalReport.getStatus();
        boolean isDelivered = ReportStatus.SUBMITTED.getDatabaseValue().equals(reportStatus)
                || ReportStatus.EVALUATED.getDatabaseValue().equals(reportStatus);

        if (!isDelivered) {
            statusLabel.setText("Estado: Reporte Final pendiente de firma/entrega");
            disableAllControls("Debes entregar (subir firmado) tu Reporte Final antes de realizar la autoevaluación.");
        } else {
            currentEvaluation = selfEvaluationManager.recoverSelfEvaluation(finalReport.getReportId());
            applyStateForEvaluation();
        }
    }

    private void applyStateForEvaluation() {
        if (currentEvaluation != null) {
            populateComboBoxes();
            statusLabel.setText("Estado de Autoevaluación: " + currentEvaluation.getStatus());
            boolean isReviewed = STATUS_REVIEWED.equals(currentEvaluation.getStatus());
            setControlsForExistingEvaluation(isReviewed);
        } else {
            statusLabel.setText("Estado: Nueva Autoevaluación habilitada");
            setControlsForNewEvaluation();
        }
    }

    private void populateComboBoxes() {
        q1ComboBox.setValue(currentEvaluation.getQ1());
        q2ComboBox.setValue(currentEvaluation.getQ2());
        q3ComboBox.setValue(currentEvaluation.getQ3());
        q4ComboBox.setValue(currentEvaluation.getQ4());
        q5ComboBox.setValue(currentEvaluation.getQ5());
        q6ComboBox.setValue(currentEvaluation.getQ6());
        q7ComboBox.setValue(currentEvaluation.getQ7());
        q8ComboBox.setValue(currentEvaluation.getQ8());
        q9ComboBox.setValue(currentEvaluation.getQ9());
        q10ComboBox.setValue(currentEvaluation.getQ10());
    }

    private void setControlsForExistingEvaluation(boolean isReviewed) {
        setComboBoxesDisabled(isReviewed);
        saveButton.setDisable(isReviewed);
        uploadSignedButton.setDisable(isReviewed);
        downloadPdfButton.setDisable(false);
    }

    private void setControlsForNewEvaluation() {
        setComboBoxesDisabled(false);
        saveButton.setDisable(false);
        uploadSignedButton.setDisable(true);
        downloadPdfButton.setDisable(true);
    }

    private void disableAllControls(String alertMessage) {
        setComboBoxesDisabled(true);
        saveButton.setDisable(true);
        downloadPdfButton.setDisable(true);
        uploadSignedButton.setDisable(true);
        Controller.showAlert(MSG_RESTRICTED, alertMessage, AlertType.INFORMATION);
    }

    private void setComboBoxesDisabled(boolean disabled) {
        q1ComboBox.setDisable(disabled);
        q2ComboBox.setDisable(disabled);
        q3ComboBox.setDisable(disabled);
        q4ComboBox.setDisable(disabled);
        q5ComboBox.setDisable(disabled);
        q6ComboBox.setDisable(disabled);
        q7ComboBox.setDisable(disabled);
        q8ComboBox.setDisable(disabled);
        q9ComboBox.setDisable(disabled);
        q10ComboBox.setDisable(disabled);
    }

    private SelfEvaluation buildEvaluationFromForm() {
        SelfEvaluation evaluation = new SelfEvaluation();
        evaluation.setPractitionerId(
                appStore.getState().sessionState().currentUserInSession().getId());
        evaluation.setReportId(finalReport.getReportId());
        evaluation.setEvidence(EVIDENCE_PENDING);
        evaluation.setQ1(q1ComboBox.getValue());
        evaluation.setQ2(q2ComboBox.getValue());
        evaluation.setQ3(q3ComboBox.getValue());
        evaluation.setQ4(q4ComboBox.getValue());
        evaluation.setQ5(q5ComboBox.getValue());
        evaluation.setQ6(q6ComboBox.getValue());
        evaluation.setQ7(q7ComboBox.getValue());
        evaluation.setQ8(q8ComboBox.getValue());
        evaluation.setQ9(q9ComboBox.getValue());
        evaluation.setQ10(q10ComboBox.getValue());
        return evaluation;
    }

    @FXML
    private void handleSaveEvaluation() {
        if (finalReport == null) {
            return;
        }

        try {
            if (currentEvaluation == null) {
                currentEvaluation = buildEvaluationFromForm();
                selfEvaluationManager.registerSelfEvaluation(currentEvaluation);
                Controller.showAlert("Éxito", MSG_SAVE_SUCCESS, AlertType.INFORMATION);
            } else {
                Controller.showAlert("Aviso", MSG_ALREADY_SAVED, AlertType.INFORMATION);
            }
            loadEvaluationState();
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleDownloadPdf() {
        if (currentEvaluation == null || currentEvaluation.getSelfEvalId() == 0) {
            Controller.showAlert("Aviso", MSG_SAVE_FIRST, AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PRAIS-03 Autoevaluación");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName(PDF_INITIAL_NAME);

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            generateAndSavePdf(file);
        }
    }

    private void generateAndSavePdf(File targetFile) {
        try {
            User currentUser = appStore.getState().sessionState().currentUserInSession();
            SelfEvaluationPdfGenerator.generateSelfEvaluationPdf(currentEvaluation, currentUser, targetFile);
            Controller.showAlert("Éxito", "PDF generado correctamente. Fírmelo y súbalo.", AlertType.INFORMATION);
        } catch (IOException e) {
            Controller.showAlert("Error", "No se pudo generar el archivo PDF.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUploadSignedPdf() {
        if (currentEvaluation == null || currentEvaluation.getSelfEvalId() == 0) {
            Controller.showAlert("Aviso", MSG_SAVE_FIRST, AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Subir PRAIS-03 Firmado");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        Stage stage = (Stage) uploadSignedButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            uploadEvidence(file.getAbsolutePath());
        }
    }

    private void uploadEvidence(String filePath) {
        try {
            selfEvaluationManager.submitEvidence(currentEvaluation.getSelfEvalId(), filePath);
            Controller.showAlert("Éxito", "Evidencia subida correctamente.", AlertType.INFORMATION);
            loadEvaluationState();
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnToMenu() {
        appStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}