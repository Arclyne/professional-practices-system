package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.DashboardManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

@Component
public class DashboardController {

    @FXML private Label systemUserNameLabel;
    @FXML private Label systemUserRoleLabel;

    @FXML private Button navigateToPractitionerManagementListButton;
    @FXML private Button navigateToPractitionerManagementMenuButton;
    @FXML private Button navigateToRegisterProfessorButton;
    @FXML private Button navigateToRegisterProjectButton;
    @FXML private Button navigateToRegisterManagerButton;
    @FXML private Button navigateToRegisterOrganizationButton;
    @FXML private Button navigateToReportGeneratorButton;
    @FXML private Button navigateToLogbookButton;
    @FXML private Button navigateToPractitionerProjectsButton;
    @FXML private Button navigateToEvaluateReportsButton;
    @FXML private Button navigateToGradePractitionerButton;
    @FXML private Button navigateToProgressReportButton;
    @FXML private Button navigateToGradeViewButton;
    @FXML private Button navigateToSelfEvaluationButton;
    @FXML private Button navigateToReviewSelfEvaluationButton;
    @FXML private Button systemLogoutButton;
    @FXML private Button navigateToMessagesButton;
    @FXML private Button navigateToRegisterPeriodButton;
    @FXML private Button navigateToRegisterPracticeGroupButton;
    @FXML private Button navigateToTemplatesButton;

    private final AppStore store;
    private final DashboardManager dashboardManager;

    @Inject
    public DashboardController(AppStore store, DashboardManager dashboardManager) {
        this.store = store;
        this.dashboardManager = dashboardManager;
    }

    @FXML
    public void initialize() {
        RootState currentState = store.getState();
        if (currentState != null && currentState.sessionState() != null) {
            User currentUser = currentState.sessionState().currentUserInSession();
            if (currentUser != null) {
                systemUserNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
                systemUserRoleLabel.setText("Rol: " + currentUser.getRole());
                adjustButtonVisibilityByRole(currentUser.getRole());
            } else {
                Controller.showAlert("Sesión inválida",
                        "No se detectó un usuario activo en el sistema. Por favor, inicie sesión nuevamente.",
                        AlertType.WARNING);
            }
        }
    }

    private void adjustButtonVisibilityByRole(String userRole) {
        hideAllNavigationButtons();

        if (dashboardManager.isCoordinatorMenuAvailable(userRole)) {
            showButton(navigateToPractitionerManagementMenuButton);
            showButton(navigateToPractitionerManagementListButton);
            showButton(navigateToRegisterProfessorButton);
            showButton(navigateToRegisterProjectButton);
            showButton(navigateToRegisterManagerButton);
            showButton(navigateToRegisterOrganizationButton);
            showButton(navigateToTemplatesButton);
            showButton(navigateToRegisterPeriodButton);
            showButton(navigateToRegisterPracticeGroupButton);

        } else if (dashboardManager.isProfessorMenuAvailable(userRole)) {
            showButton(navigateToEvaluateReportsButton);
            showButton(navigateToGradePractitionerButton);
            showButton(navigateToReviewSelfEvaluationButton);

        } else if (dashboardManager.isPractitionerMenuAvailable(userRole)) {
            showButton(navigateToPractitionerProjectsButton);
            showButton(navigateToLogbookButton);
            showButton(navigateToReportGeneratorButton);
            showButton(navigateToProgressReportButton);
            showButton(navigateToGradeViewButton);
            showButton(navigateToSelfEvaluationButton);
        }
    }

    private void showButton(Button button) {
        if (button != null) {
            button.setVisible(true);
            button.setManaged(true);
        }
    }

    private void hideButton(Button button) {
        if (button != null) {
            button.setVisible(false);
            button.setManaged(false);
        }
    }

    private void hideAllNavigationButtons() {
        hideButton(navigateToPractitionerManagementListButton);
        hideButton(navigateToPractitionerManagementMenuButton);
        hideButton(navigateToRegisterProfessorButton);
        hideButton(navigateToRegisterProjectButton);
        hideButton(navigateToRegisterManagerButton);
        hideButton(navigateToRegisterOrganizationButton);
        hideButton(navigateToPractitionerProjectsButton);
        hideButton(navigateToRegisterPeriodButton);
        hideButton(navigateToRegisterPracticeGroupButton);
        hideButton(navigateToTemplatesButton);
        hideButton(navigateToLogbookButton);
        hideButton(navigateToReportGeneratorButton);
        hideButton(navigateToEvaluateReportsButton);
        hideButton(navigateToGradePractitionerButton);
        hideButton(navigateToProgressReportButton);
        hideButton(navigateToGradeViewButton);
        hideButton(navigateToSelfEvaluationButton);
        hideButton(navigateToReviewSelfEvaluationButton);
    }

    @FXML
    private void handleNavigateToPractitionerManagementAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
    }

    @FXML
    private void handleNavigateToPractitionerManagementListAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterProfessorAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterProjectAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PROJECT_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterManagerAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.MANAGER_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterOrganizationAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.ORGANIZATION_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToPractitionerProjectsAction() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        try {
            AppSection targetSection = dashboardManager.resolvePractitionerProjectsNavigation(currentUser.getId());
            store.dispatch(new NavigationAction.GoToSection(targetSection));
        } catch (ManagerException e) {
            Controller.showAlert("Acceso denegado", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleNavigateToRegisterPeriodAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PERIOD));
    }

    @FXML
    private void handleNavigateToRegisterPracticeGroupAction( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTICE_GROUP));
    }

    @FXML
    private void handleNavigateToMessagesAction( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.MESSAGES));
    }

    @FXML
    private void handleSystemLogoutAction( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }

    @FXML
    private void handleNavigateToLogbookAction( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_LOGBOOK));
    }

    @FXML
    private void handleNavigateToReportsList( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_REPORTS_LIST));
    }

    @FXML
    private void handleNavigateToEvaluateReportsAction( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_EVALUATE_REPORT));
    }

    @FXML
    private void handleNavigateToTemplatesAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.TEMPLATE_GENERATOR));
    }

    @FXML
    private void handleNavigateToGradePractitionerAction( ) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.GRADE_PRACTITIONER));
    }

    @FXML
    private void handleNavigateToProgressReportAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PROGRESS_REPORT_GENERATOR));
    }

    @FXML
    private void handleNavigateToGradeViewAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_GRADE_VIEW));
    }

    @FXML
    private void handleNavigateToSelfEvaluationAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_SELF_EVALUATION));
    }

    @FXML
    private void handleNavigateToReviewSelfEvaluationAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_REVIEW_SELF_EVALUATION));
    }
}