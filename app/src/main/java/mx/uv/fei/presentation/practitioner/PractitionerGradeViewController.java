package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.evaluation.GradingManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

@Component
public class PractitionerGradeViewController {

    private static final String GRADE_FORMAT = "%.2f / 10.0";
    private static final String NO_ACTIVE_PERIOD_TEXT = "Periodo: No disponible";

    @FXML private Label labelFinalGrade;
    @FXML private Label labelPeriod;
    @FXML private VBox finalGradeContainer;
    @FXML private VBox noGradeYetContainer;

    private final GradingManager gradingManager;
    private final PeriodManager periodManager;
    private final AppStore store;

    @Inject
    public PractitionerGradeViewController(GradingManager gradingManager, PeriodManager periodManager, AppStore store) {
        this.gradingManager = gradingManager;
        this.periodManager = periodManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        int practitionerId = currentUser != null ? currentUser.getId() : 0;
        loadActivePeriodGrade(practitionerId);
    }

    private void loadActivePeriodGrade(int practitionerId) {
        try {
            Period activePeriod = periodManager.getActivePeriod();
            if (activePeriod != null) {
                labelPeriod.setText("Periodo: " + activePeriod.getPeriodName());
                loadGrade(practitionerId, activePeriod.getPeriodName());
            } else {
                labelPeriod.setText(NO_ACTIVE_PERIOD_TEXT);
                displayGradeState(false);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadGrade(int practitionerId, String period) {
        try {
            PractitionerGrade practitionerGrade = gradingManager
                    .getGradeByPractitionerAndPeriod(practitionerId, period);
            boolean hasGrade = practitionerGrade != null && practitionerGrade.getFinalGrade() != null;
            displayGradeState(hasGrade);

            if (hasGrade) {
                labelFinalGrade.setText(String.format(GRADE_FORMAT, practitionerGrade.getFinalGrade()));
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    private void displayGradeState(boolean hasGrade) {
        finalGradeContainer.setVisible(hasGrade);
        finalGradeContainer.setManaged(hasGrade);
        noGradeYetContainer.setVisible(!hasGrade);
        noGradeYetContainer.setManaged(!hasGrade);
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}