package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.GradingManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerGradeViewController {

    private static final String PERIOD_ACTIVE  = "Junio-Diciembre 2026";
    private static final String GRADE_FORMAT   = "%.2f / 10.0";

    @FXML private Label labelTentativeGrade;
    @FXML private Label labelFinalGrade;
    @FXML private Label labelPeriod;
    @FXML private VBox finalGradeContainer;
    @FXML private Label labelNoGradeYet;

    private final GradingManager gradingManager;
    private final AppStore store;

    @Inject
    public PractitionerGradeViewController(GradingManager gradingManager, AppStore store) {
        this.gradingManager = gradingManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        User currentPractitioner = store.getState().sessionState().currentUserInSession();
        int practitionerId = currentPractitioner != null ? currentPractitioner.getId() : 0;

        labelPeriod.setText("Periodo: " + PERIOD_ACTIVE);

        loadTentativeGrade(practitionerId);
        loadFinalGrade(practitionerId);
    }

    private void loadTentativeGrade(int practitionerId) {
        try {
            double tentative = gradingManager.previewTentativeGrade(practitionerId);
            labelTentativeGrade.setText(String.format(GRADE_FORMAT, tentative));
        } catch (ManagerException exception) {
            labelTentativeGrade.setText("No disponible");
            Controller.showAlert("Error", exception.getMessage(), AlertType.ERROR);
        }
    }

    private void loadFinalGrade(int practitionerId) {
        try {
            PractitionerGrade grade = gradingManager
                    .getGradeByPractitionerAndPeriod(practitionerId, PERIOD_ACTIVE);

            boolean hasFinalGrade = grade != null && grade.getFinalGrade() != null;

            finalGradeContainer.setVisible(hasFinalGrade);
            finalGradeContainer.setManaged(hasFinalGrade);
            labelNoGradeYet.setVisible(!hasFinalGrade);
            labelNoGradeYet.setManaged(!hasFinalGrade);

            if (hasFinalGrade) {
                labelFinalGrade.setText(String.format(GRADE_FORMAT, grade.getFinalGrade()));
            }
        } catch (ManagerException exception) {
            Controller.showAlert("Error", exception.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
