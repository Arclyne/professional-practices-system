package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.SelfEvaluationPdfGenerator;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.io.File;
import java.io.IOException;

@Component
public class PractitionerSelfEvaluationController {

    @FXML private TextField reportIdField;
    @FXML private Label statusLabel;

    @FXML private ComboBox<Integer> cbQ1, cbQ2, cbQ3, cbQ4, cbQ5, cbQ6, cbQ7, cbQ8, cbQ9, cbQ10;

    @FXML private Button saveButton;
    @FXML private Button downloadPdfButton;
    @FXML private Button uploadSignedButton;

    private final SelfEvaluationManager selfEvaluationManager;
    private final AppStore appStore;

    private SelfEvaluation currentEvaluation;

    @Inject
    public PractitionerSelfEvaluationController(SelfEvaluationManager selfEvaluationManager, AppStore appStore) {
        this.selfEvaluationManager = selfEvaluationManager;
        this.appStore = appStore;
    }

    @FXML
    public void initialize() {
        ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 3, 4, 5);
        ComboBox[] combos = {cbQ1, cbQ2, cbQ3, cbQ4, cbQ5, cbQ6, cbQ7, cbQ8, cbQ9, cbQ10};
        for (ComboBox<Integer> cb : combos) {
            cb.setItems(options);
            cb.setValue(1);
        }
    }

    @FXML
    private void handleSearchReport() {
        try {
            int reportId = Integer.parseInt(reportIdField.getText().trim());
            currentEvaluation = selfEvaluationManager.recoverSelfEvaluation(reportId);

            if (currentEvaluation != null) {
                cbQ1.setValue(currentEvaluation.getQ1()); cbQ2.setValue(currentEvaluation.getQ2());
                cbQ3.setValue(currentEvaluation.getQ3()); cbQ4.setValue(currentEvaluation.getQ4());
                cbQ5.setValue(currentEvaluation.getQ5()); cbQ6.setValue(currentEvaluation.getQ6());
                cbQ7.setValue(currentEvaluation.getQ7()); cbQ8.setValue(currentEvaluation.getQ8());
                cbQ9.setValue(currentEvaluation.getQ9()); cbQ10.setValue(currentEvaluation.getQ10());

                statusLabel.setText("Estado: " + currentEvaluation.getStatus());

                if ("Revisada".equals(currentEvaluation.getStatus())) {
                    disableComboBoxes(true);
                    saveButton.setDisable(true);
                    uploadSignedButton.setDisable(true);
                } else {
                    disableComboBoxes(false);
                    saveButton.setDisable(false);
                    uploadSignedButton.setDisable(false);
                }
            } else {
                statusLabel.setText("Estado: Nueva Autoevaluación");
                disableComboBoxes(false);
                saveButton.setDisable(false);
            }
        } catch (NumberFormatException e) {
            Controller.showAlert("Error", "Ingrese un ID de reporte válido (número).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    private void disableComboBoxes(boolean disable) {
        ComboBox[] combos = {cbQ1, cbQ2, cbQ3, cbQ4, cbQ5, cbQ6, cbQ7, cbQ8, cbQ9, cbQ10};
        for (ComboBox<Integer> cb : combos) cb.setDisable(disable);
    }

    @FXML
    private void handleSaveEvaluation() {
        if(reportIdField.getText().trim().isEmpty()) {
            Controller.showAlert("Aviso", "Primero ingrese el ID de su reporte final y pulse Buscar.", AlertType.WARNING);
            return;
        }

        try {
            int reportId = Integer.parseInt(reportIdField.getText().trim());
            if (currentEvaluation == null) {
                currentEvaluation = new SelfEvaluation();
                currentEvaluation.setPractitionerId(appStore.getState().sessionState().currentUserInSession().getId());
                currentEvaluation.setReportId(reportId);
                currentEvaluation.setEvidence("pendiente");

                currentEvaluation.setQ1(cbQ1.getValue()); currentEvaluation.setQ2(cbQ2.getValue());
                currentEvaluation.setQ3(cbQ3.getValue()); currentEvaluation.setQ4(cbQ4.getValue());
                currentEvaluation.setQ5(cbQ5.getValue()); currentEvaluation.setQ6(cbQ6.getValue());
                currentEvaluation.setQ7(cbQ7.getValue()); currentEvaluation.setQ8(cbQ8.getValue());
                currentEvaluation.setQ9(cbQ9.getValue()); currentEvaluation.setQ10(cbQ10.getValue());

                selfEvaluationManager.registerSelfEvaluation(currentEvaluation);
                Controller.showAlert("Éxito", "Autoevaluación guardada correctamente.", AlertType.INFORMATION);
            } else {
                Controller.showAlert("Aviso", "La autoevaluación ya estaba registrada.", AlertType.INFORMATION);
            }
            handleSearchReport();
        } catch (NumberFormatException e) {
            Controller.showAlert("Error", "ID de reporte inválido.", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleDownloadPdf() {
        if (currentEvaluation == null || currentEvaluation.getSelfEvalId() == 0) {
            Controller.showAlert("Aviso", "Primero debe Guardar su evaluación.", AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PRAIS-03 Autoevaluación");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName("PRAIS-03_Autoevaluacion.pdf");

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                User me = appStore.getState().sessionState().currentUserInSession();
                SelfEvaluationPdfGenerator.generateSelfEvaluationPdf(currentEvaluation, me, file);
                Controller.showAlert("Éxito", "PDF generado correctamente. Fírmelo y súbalo.", AlertType.INFORMATION);
            } catch (IOException e) {
                Controller.showAlert("Error", "No se pudo generar el archivo PDF.", AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleUploadSignedPdf() {
        if (currentEvaluation == null || currentEvaluation.getSelfEvalId() == 0) {
            Controller.showAlert("Aviso", "Primero debe Guardar su evaluación.", AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Subir PRAIS-03 Firmado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        Stage stage = (Stage) uploadSignedButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                String fileUrl = file.getAbsolutePath();
                selfEvaluationManager.submitEvidence(currentEvaluation.getSelfEvalId(), fileUrl);
                Controller.showAlert("Éxito", "Evidencia subida correctamente.", AlertType.INFORMATION);
                handleSearchReport();
            } catch (ManagerException e) {
                Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleReturnToMenu() {
        appStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}