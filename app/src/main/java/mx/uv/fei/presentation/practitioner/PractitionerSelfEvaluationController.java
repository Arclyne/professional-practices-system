package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.SelfEvaluationPdfGenerator;
import mx.uv.fei.domain.common.validators.FileValidator;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.enums.SelfEvaluationStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.manager.evaluation.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class PractitionerSelfEvaluationController {

    private static final String REPORT_TYPE_FINAL = "Final";
    private static final String PROGRESS_REPORT_VIEW = "/mx/uv/fei/presentation/progressReportGenerator.fxml";
    private static final String SELF_EVALUATION_STATUS_REVIEWED = SelfEvaluationStatus.REVIEWED.getDatabaseValue();
    private static final String SELF_EVALUATION_STATUS_REJECTED = SelfEvaluationStatus.REJECTED.getDatabaseValue();
    private static final String EVIDENCE_PENDING = "pendiente";
    private static final String PDF_INITIAL_NAME = "PRAIS-03_Autoevaluacion.pdf";
    private static final int MINIMUM_SCORE = 1;
    private static final int MAXIMUM_SCORE = 5;

    @FXML private Label statusLabel;
    @FXML private Label requirementLabel;
    @FXML private VBox formContainer;
    @FXML private VBox requirementContainer;
    @FXML private Button goToReportButton;
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
    private final ShellNavigator shellNavigator;
    private final AppStore store;

    private SelfEvaluation currentSelfEvaluation;
    private ProgressReport finalReport;

    @Inject
    public PractitionerSelfEvaluationController(SelfEvaluationManager selfEvaluationManager,
                                                ProgressReportManager progressReportManager,
                                                ShellNavigator shellNavigator, AppStore store) {
        this.selfEvaluationManager = selfEvaluationManager;
        this.progressReportManager = progressReportManager;
        this.shellNavigator = shellNavigator;
        this.store = store;
    }

    @FXML
    public void initialize() {
        ObservableList<Integer> scoreOptions = buildScoreOptions();
        forEachQuestionComboBox(comboBox -> initializeComboBox(comboBox, scoreOptions));

        showFormContainer(false);
        showRequirementContainer(false);
        loadEvaluationState();
    }

    private ObservableList<Integer> buildScoreOptions() {
        ObservableList<Integer> options = FXCollections.observableArrayList();
        for (int score = MINIMUM_SCORE; score <= MAXIMUM_SCORE; score++) {
            options.add(score);
        }
        return options;
    }

    private void initializeComboBox(ComboBox<Integer> comboBox, ObservableList<Integer> options) {
        comboBox.setItems(options);
        comboBox.setValue(MINIMUM_SCORE);
    }

    private void showFormContainer(boolean isVisible) {
        formContainer.setVisible(isVisible);
        formContainer.setManaged(isVisible);
    }

    private void showRequirementContainer(boolean isVisible) {
        requirementContainer.setVisible(isVisible);
        requirementContainer.setManaged(isVisible);
    }

    private void loadEvaluationState() {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            List<ProgressReport> reports = progressReportManager
                    .getProgressReportsByPractitioner(currentUser.getId());

            finalReport = reports.stream()
                    .filter(report -> REPORT_TYPE_FINAL.equals(report.getReportType()))
                    .findFirst()
                    .orElse(null);

            applyStateForFinalReport();
        } catch (ManagerException e) {
            showBlockedState("Ocurrió un error al verificar los requisitos: " + e.getMessage(), false);
        }
    }

    private void applyStateForFinalReport() throws ManagerException {
        if (finalReport == null) {
            showBlockedState(
                    "Para realizar tu autoevaluación primero debes generar y entregar tu Reporte Final de Prácticas.",
                    true);
            statusLabel.setText("Requisito: Reporte Final no generado");
        } else {
            applyStateForReportStatus();
        }
    }

    private void applyStateForReportStatus() throws ManagerException {
        if (!isFinalReportAccepted()) {
            showBlockedState(
                    "Tu Reporte Final aún no ha sido aceptado por tu profesor. "
                            + "Podrás realizar tu autoevaluación cuando el reporte sea evaluado y aceptado.",
                    true);
            statusLabel.setText("Requisito pendiente: que tu profesor acepte el Reporte Final");
        } else {
            currentSelfEvaluation = selfEvaluationManager.recoverSelfEvaluation(finalReport.getReportId());
            showFormContainer(true);
            showRequirementContainer(false);
            applyStateForEvaluation();
        }
    }

    private boolean isFinalReportAccepted() {
        return ReportStatus.EVALUATED.getDatabaseValue().equals(finalReport.getStatus());
    }

    private void showBlockedState(String message, boolean showNavigationButton) {
        showFormContainer(false);
        showRequirementContainer(true);
        requirementLabel.setText(message);
        goToReportButton.setVisible(showNavigationButton);
        goToReportButton.setManaged(showNavigationButton);
    }

    private void applyStateForEvaluation() {
        if (currentSelfEvaluation != null) {
            populateComboBoxes();
            statusLabel.setText(buildExistingStatusText());
            boolean isReviewed = SELF_EVALUATION_STATUS_REVIEWED.equals(currentSelfEvaluation.getStatus());
            setControlsForExistingEvaluation(isReviewed);
        } else {
            statusLabel.setText("Autoevaluación habilitada — completa el formulario");
            setControlsForNewEvaluation();
        }
    }

    private String buildExistingStatusText() {
        String statusText = "Estado de Autoevaluación: " + currentSelfEvaluation.getStatus();
        boolean isRejected = SELF_EVALUATION_STATUS_REJECTED.equals(currentSelfEvaluation.getStatus());
        if (isRejected && currentSelfEvaluation.getReviewComment() != null) {
            statusText = statusText + " — Motivo: " + currentSelfEvaluation.getReviewComment();
        }
        return statusText;
    }

    private void populateComboBoxes() {
        q1ComboBox.setValue(currentSelfEvaluation.getQ1());
        q2ComboBox.setValue(currentSelfEvaluation.getQ2());
        q3ComboBox.setValue(currentSelfEvaluation.getQ3());
        q4ComboBox.setValue(currentSelfEvaluation.getQ4());
        q5ComboBox.setValue(currentSelfEvaluation.getQ5());
        q6ComboBox.setValue(currentSelfEvaluation.getQ6());
        q7ComboBox.setValue(currentSelfEvaluation.getQ7());
        q8ComboBox.setValue(currentSelfEvaluation.getQ8());
        q9ComboBox.setValue(currentSelfEvaluation.getQ9());
        q10ComboBox.setValue(currentSelfEvaluation.getQ10());
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

    private void setComboBoxesDisabled(boolean isDisabled) {
        forEachQuestionComboBox(comboBox -> comboBox.setDisable(isDisabled));
    }

    private void forEachQuestionComboBox(java.util.function.Consumer<ComboBox<Integer>> action) {
        List<ComboBox<Integer>> comboBoxes = List.of(
                q1ComboBox, q2ComboBox, q3ComboBox, q4ComboBox, q5ComboBox,
                q6ComboBox, q7ComboBox, q8ComboBox, q9ComboBox, q10ComboBox);
        comboBoxes.forEach(action);
    }

    private SelfEvaluation buildEvaluationFromForm() {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setPractitionerId(store.getState().sessionState().currentUserInSession().getId());
        selfEvaluation.setReportId(finalReport.getReportId());
        selfEvaluation.setEvidence(EVIDENCE_PENDING);
        selfEvaluation.setQ1(q1ComboBox.getValue());
        selfEvaluation.setQ2(q2ComboBox.getValue());
        selfEvaluation.setQ3(q3ComboBox.getValue());
        selfEvaluation.setQ4(q4ComboBox.getValue());
        selfEvaluation.setQ5(q5ComboBox.getValue());
        selfEvaluation.setQ6(q6ComboBox.getValue());
        selfEvaluation.setQ7(q7ComboBox.getValue());
        selfEvaluation.setQ8(q8ComboBox.getValue());
        selfEvaluation.setQ9(q9ComboBox.getValue());
        selfEvaluation.setQ10(q10ComboBox.getValue());
        return selfEvaluation;
    }

    @FXML
    private void handleSaveEvaluation() {
        if (finalReport != null) {
            saveCurrentEvaluation();
        }
    }

    private void saveCurrentEvaluation() {
        try {
            if (currentSelfEvaluation == null) {
                currentSelfEvaluation = buildEvaluationFromForm();
                selfEvaluationManager.registerSelfEvaluation(currentSelfEvaluation);
                Controller.showAlert("Éxito",
                        "Autoevaluación guardada. Ahora puede descargar el PDF para firmarlo.",
                        AlertType.INFORMATION);
            } else {
                Controller.showAlert("Aviso", "La autoevaluación ya estaba registrada.", AlertType.INFORMATION);
            }
            loadEvaluationState();
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleDownloadPdf() {
        if (isSelfEvaluationUnsaved()) {
            Controller.showAlert("Aviso", "Primero debe guardar su evaluación.", AlertType.WARNING);
        } else {
            chooseAndGeneratePdf();
        }
    }

    private void chooseAndGeneratePdf() {
        FileChooser fileChooser = buildPdfFileChooser("Guardar PRAIS-03 Autoevaluación");
        fileChooser.setInitialFileName(PDF_INITIAL_NAME);
        File file = fileChooser.showSaveDialog(currentStage(saveButton));
        if (file != null) {
            generateAndSavePdf(file);
        }
    }

    private void generateAndSavePdf(File targetFile) {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            SelfEvaluationPdfGenerator.generateSelfEvaluationPdf(currentSelfEvaluation, currentUser, targetFile);
            Controller.showAlert("Éxito", "PDF generado. Fírmelo y súbalo.", AlertType.INFORMATION);
        } catch (IOException e) {
            Controller.showAlert("Error", "No se pudo generar el archivo PDF.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleUploadSignedPdf() {
        if (isSelfEvaluationUnsaved()) {
            Controller.showAlert("Aviso", "Primero debe guardar su evaluación.", AlertType.WARNING);
        } else {
            chooseAndSubmitSignedPdf();
        }
    }

    private void chooseAndSubmitSignedPdf() {
        FileChooser fileChooser = buildPdfFileChooser("Subir PRAIS-03 Firmado");
        File file = fileChooser.showOpenDialog(currentStage(uploadSignedButton));
        if (file != null) {
            submitSelectedEvidence(file);
        }
    }

    private void submitSelectedEvidence(File file) {
        if (isFileSizeValid(file)) {
            uploadEvidence(file.getAbsolutePath());
        }
    }

    private boolean isFileSizeValid(File file) {
        boolean isValid = false;

        try {
            FileValidator.validateFileSize(file);
            isValid = true;
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }

        return isValid;
    }

    private void uploadEvidence(String filePath) {
        try {
            selfEvaluationManager.submitEvidence(currentSelfEvaluation.getSelfEvalId(), filePath);
            Controller.showAlert("Éxito", "Evidencia subida correctamente.", AlertType.INFORMATION);
            loadEvaluationState();
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean isSelfEvaluationUnsaved() {
        return currentSelfEvaluation == null || currentSelfEvaluation.getSelfEvalId() == 0;
    }

    private FileChooser buildPdfFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        return fileChooser;
    }

    private Stage currentStage(Button button) {
        return (Stage) button.getScene().getWindow();
    }

    @FXML
    private void handleGoToReport() {
        shellNavigator.showSubView(PROGRESS_REPORT_VIEW);
    }
}