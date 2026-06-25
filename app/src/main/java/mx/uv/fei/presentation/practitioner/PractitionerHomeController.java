package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.PracticeStatus;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PracticeAccessManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.evaluation.GradingManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.manager.people.ProfessorManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
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
    @FXML private Label statusBannerLabel;
    @FXML private Label accumulatedHoursLabel;
    @FXML private Label partialGradeLabel;
    @FXML private Label sectionLabel;
    @FXML private Label professorLabel;

    private final AppStore store;
    private final ProgressReportManager progressReportManager;
    private final GradingManager gradingManager;
    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final ProfessorManager professorManager;
    private final PracticeAccessManager practiceAccessManager;

    @Inject
    public PractitionerHomeController(AppStore store, ProgressReportManager progressReportManager,
                                      GradingManager gradingManager, PractitionerManager practitionerManager,
                                      PracticeGroupManager practiceGroupManager, ProfessorManager professorManager,
                                      PracticeAccessManager practiceAccessManager) {
        this.store = store;
        this.progressReportManager = progressReportManager;
        this.gradingManager = gradingManager;
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.professorManager = professorManager;
        this.practiceAccessManager = practiceAccessManager;
    }

    @FXML
    public void initialize() {
        User currentUser = resolveCurrentUser();
        populateGreeting(currentUser);
        populateStatusBanner(currentUser);
        populateAccumulatedHours(currentUser);
        populatePartialGrade(currentUser);
        populateGroupInfo(currentUser);
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

    private void populateStatusBanner(User currentUser) {
        setStatusBannerVisible(false);
        if (currentUser == null) {
            return;
        }

        try {
            PracticeStatus practiceStatus = practiceAccessManager.resolveStatus(currentUser.getId());
            applyStatusBanner(practiceAccessManager.buildHomeStatusMessage(practiceStatus));
        } catch (ManagerException e) {
            setStatusBannerVisible(false);
        }
    }

    private void applyStatusBanner(String bannerMessage) {
        boolean hasMessage = !bannerMessage.isEmpty();
        statusBannerLabel.setText(bannerMessage);
        setStatusBannerVisible(hasMessage);
    }

    private void setStatusBannerVisible(boolean isVisible) {
        statusBannerLabel.setVisible(isVisible);
        statusBannerLabel.setManaged(isVisible);
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

    private void populateGroupInfo(User currentUser) {
        sectionLabel.setText(UNAVAILABLE_METRIC);
        professorLabel.setText(UNAVAILABLE_METRIC);

        if (currentUser == null) {
            return;
        }

        try {
            applyGroupInfo(currentUser.getId());
        } catch (ManagerException e) {
            sectionLabel.setText(UNAVAILABLE_METRIC);
            professorLabel.setText(UNAVAILABLE_METRIC);
        }
    }

    private void applyGroupInfo(int practitionerId) throws ManagerException {
        Practitioner practitioner = practitionerManager.getPractitionerById(practitionerId);
        Integer groupId = practitioner != null ? practitioner.getGroupId() : null;
        if (groupId == null || groupId <= 0) {
            return;
        }

        PracticeGroup group = practiceGroupManager.getPracticeGroupById(groupId);
        if (group == null || group.getGroupId() <= 0) {
            return;
        }

        sectionLabel.setText(group.getSection());
        professorLabel.setText(resolveProfessorName(group.getProfessorId()));
    }

    private String resolveProfessorName(int professorId) throws ManagerException {
        Professor professor = professorManager.getProfessorById(professorId);
        if (professor == null || professor.getName() == null) {
            return UNAVAILABLE_METRIC;
        }
        return professor.getName() + " " + professor.getLastName();
    }
}
