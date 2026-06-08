package mx.uv.fei.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

@Component
public class ReviewSelfEvaluationController {

    @FXML private TextField reportIdField;
    @FXML private Label scoreLabel;
    @FXML private Label currentStatusLabel;
    @FXML private Button viewPdfButton;
    @FXML private Button markAsReviewedButton;

    private final SelfEvaluationManager selfEvaluationManager;
    private final AppStore appStore;
    private SelfEvaluation targetEvaluation;

    @Inject
    public ReviewSelfEvaluationController(SelfEvaluationManager selfEvaluationManager, AppStore appStore) {
        this.selfEvaluationManager = selfEvaluationManager;
        this.appStore = appStore;
    }

    @FXML
    public void initialize() {
        viewPdfButton.setDisable(true);
        markAsReviewedButton.setDisable(true);
    }

    @FXML
    private void handleSearchEvaluation() {
        try {
            int reportId = Integer.parseInt(reportIdField.getText().trim());
            targetEvaluation = selfEvaluationManager.recoverSelfEvaluation(reportId);

            if (targetEvaluation != null) {
                scoreLabel.setText("Puntuación Final obtenida: " + targetEvaluation.getTotalScore() + " / 50");
                currentStatusLabel.setText("Estado actual: " + targetEvaluation.getStatus());

                boolean hasEvidence = targetEvaluation.getEvidence() != null && !targetEvaluation.getEvidence().equals("pendiente");
                viewPdfButton.setDisable(!hasEvidence);

                boolean canReview = hasEvidence && !"Revisada".equals(targetEvaluation.getStatus());
                markAsReviewedButton.setDisable(!canReview);
            }
        } catch (NumberFormatException e) {
            Controller.showAlert("Error", "Ingrese un ID de reporte válido.", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("No encontrado", "No se encontró autoevaluación para este reporte.", AlertType.INFORMATION);
            scoreLabel.setText("Puntuación Final obtenida: -");
            viewPdfButton.setDisable(true);
            markAsReviewedButton.setDisable(true);
        }
    }

    @FXML
    private void handleViewPdf() {
        if (targetEvaluation != null && targetEvaluation.getEvidence() != null) {
            File pdfFile = new File(targetEvaluation.getEvidence());
            if (pdfFile.exists()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (IOException e) {
                    Controller.showAlert("Error", "No se pudo abrir el archivo PDF.", AlertType.ERROR);
                }
            } else {
                Controller.showAlert("Archivo no encontrado", "La ruta de la evidencia no es válida.", AlertType.WARNING);
            }
        }
    }

    @FXML
    private void handleMarkAsReviewed() {
        if (targetEvaluation != null) {
            try {
                selfEvaluationManager.markAsReviewed(targetEvaluation.getReportId());
                Controller.showAlert("Éxito", "La autoevaluación ha sido marcada como revisada.", AlertType.INFORMATION);
                handleSearchEvaluation();
            } catch (ManagerException e) {
                Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleReturnToMenu() {
        appStore.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_MANAGEMENT_MENU)); // Ajusta el Enum a tu menú de profesor
    }
}