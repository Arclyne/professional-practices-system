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
    private static final String LOAD_ERROR_TEXT = "Error al cargar";
    private static final String HOURS_FORMAT = "%.0f h";
    private static final String EMPTY_BANNER = "";

    @FXML private Label greetingLabel;
    @FXML private Label statusBannerLabel;
    @FXML private Label accumulatedHoursLabel;
    @FXML private Label sectionLabel;
    @FXML private Label professorLabel;

    private final AppStore store;
    private final ProgressReportManager progressReportManager;
    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final ProfessorManager professorManager;
    private final PracticeAccessManager practiceAccessManager;

    @Inject
    public PractitionerHomeController(AppStore store, ProgressReportManager progressReportManager,
                                      PractitionerManager practitionerManager,
                                      PracticeGroupManager practiceGroupManager, ProfessorManager professorManager,
                                      PracticeAccessManager practiceAccessManager) {
        this.store = store;
        this.progressReportManager = progressReportManager;
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
        String bannerMessage = EMPTY_BANNER;

        if (currentUser != null) {
            bannerMessage = resolveStatusBannerMessage(currentUser.getId());
        }

        boolean hasMessage = !bannerMessage.isEmpty();
        statusBannerLabel.setText(bannerMessage);
        setStatusBannerVisible(hasMessage);
    }

    private String resolveStatusBannerMessage(int practitionerId) {
        String bannerMessage;

        try {
            PracticeStatus practiceStatus = practiceAccessManager.resolveStatus(practitionerId);
            bannerMessage = practiceAccessManager.buildHomeStatusMessage(practiceStatus);
        } catch (ManagerException e) {
            bannerMessage = EMPTY_BANNER;
        }

        return bannerMessage;
    }

    private void setStatusBannerVisible(boolean isVisible) {
        statusBannerLabel.setVisible(isVisible);
        statusBannerLabel.setManaged(isVisible);
    }

    private void populateAccumulatedHours(User currentUser) {
        String hoursText = UNAVAILABLE_METRIC;

        if (currentUser != null) {
            hoursText = resolveAccumulatedHoursText(currentUser.getId());
        }

        accumulatedHoursLabel.setText(hoursText);
    }

    private String resolveAccumulatedHoursText(int practitionerId) {
        String hoursText;

        try {
            double accumulatedHours = progressReportManager.getAccumulatedHours(practitionerId);
            hoursText = String.format(HOURS_FORMAT, accumulatedHours);
        } catch (ManagerException e) {
            hoursText = LOAD_ERROR_TEXT;
        }

        return hoursText;
    }

    private void populateGroupInfo(User currentUser) {
        String sectionText = UNAVAILABLE_METRIC;
        String professorText = UNAVAILABLE_METRIC;

        if (currentUser != null) {
            try {
                PracticeGroup group = resolveCurrentGroup(currentUser.getId());
                if (group != null && group.getGroupId() > 0) {
                    sectionText = group.getSection();
                    professorText = resolveProfessorName(group.getProfessorId());
                }
            } catch (ManagerException e) {
                sectionText = LOAD_ERROR_TEXT;
                professorText = LOAD_ERROR_TEXT;
            }
        }

        sectionLabel.setText(sectionText);
        professorLabel.setText(professorText);
    }

    private PracticeGroup resolveCurrentGroup(int practitionerId) throws ManagerException {
        PracticeGroup group = null;
        Practitioner practitioner = practitionerManager.getPractitionerById(practitionerId);
        Integer groupId = practitioner != null ? practitioner.getGroupId() : null;

        if (groupId != null && groupId > 0) {
            group = practiceGroupManager.getPracticeGroupById(groupId);
        }

        return group;
    }

    private String resolveProfessorName(int professorId) throws ManagerException {
        Professor professor = professorManager.getProfessorById(professorId);
        String professorName = UNAVAILABLE_METRIC;

        if (professor != null && professor.getName() != null) {
            professorName = professor.getName() + " " + professor.getLastName();
        }

        return professorName;
    }
}
