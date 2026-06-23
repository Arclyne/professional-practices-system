package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.GradingManager;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Component
public class PractitionerHomeController {

    private static final String DEFAULT_GREETING_NAME = "practicante";
    private static final String UNAVAILABLE_METRIC = "—";
    private static final String HOURS_FORMAT = "%.0f h";
    private static final String GRADE_FORMAT = "%.1f / 10";

    @FXML private Label greetingLabel;
    @FXML private Label accumulatedHoursLabel;
    @FXML private Label partialGradeLabel;

    private final AppStore store;
    private final ProgressReportManager progressReportManager;
    private final GradingManager gradingManager;

    @Inject
    public PractitionerHomeController(AppStore store, ProgressReportManager progressReportManager,
                                      GradingManager gradingManager) {
        this.store = store;
        this.progressReportManager = progressReportManager;
        this.gradingManager = gradingManager;
    }

    @FXML
    public void initialize() {
        User currentUser = resolveCurrentUser();
        populateGreeting(currentUser);
        populateAccumulatedHours(currentUser);
        populatePartialGrade(currentUser);
    }

    private User resolveCurrentUser() {
        User currentUser = null;
        RootState currentState = store.getState();

        if (currentState != null && currentState.sessionState() != null) {
            currentUser = currentState.sessionState().currentUserInSession();
        }

        return currentUser;
    }

    private void populateGreeting(User currentUser) {
        String displayName = currentUser != null ? currentUser.getName() : DEFAULT_GREETING_NAME;
        greetingLabel.setText("Hola, " + displayName);
    }

    private void populateAccumulatedHours(User currentUser) {
        String hoursText = UNAVAILABLE_METRIC;

        if (currentUser != null) {
            try {
                double accumulatedHours = progressReportManager.getAccumulatedHours(currentUser.getId());
                hoursText = String.format(HOURS_FORMAT, accumulatedHours);
            } catch (ManagerException e) {
                hoursText = UNAVAILABLE_METRIC;
            }
        }

        accumulatedHoursLabel.setText(hoursText);
    }

    private void populatePartialGrade(User currentUser) {
        String gradeText = UNAVAILABLE_METRIC;

        if (currentUser != null) {
            try {
                double partialGrade = gradingManager.previewTentativeGrade(currentUser.getId());
                gradeText = String.format(GRADE_FORMAT, partialGrade);
            } catch (ManagerException e) {
                gradeText = UNAVAILABLE_METRIC;
            }
        }

        partialGradeLabel.setText(gradeText);
    }
}
