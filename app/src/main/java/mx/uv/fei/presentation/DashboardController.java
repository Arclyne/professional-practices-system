package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.domain.manager.DashboardManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

@Component
public class DashboardController {

    @FXML private Label systemUserNameLabel;
    @FXML private Label systemUserRoleLabel;

    @FXML private Button navigateToRegisterCoordinatorButton;
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

    private final AppStore applicationNavigationStore;
    private final DashboardManager applicationDashboardManager;
    private final CoordinatorManager coordinatorManager;

    @Inject
    public DashboardController(AppStore applicationNavigationStore, DashboardManager applicationDashboardManager, CoordinatorManager coordinatorManager) {
        this.applicationNavigationStore = applicationNavigationStore;
        this.applicationDashboardManager = applicationDashboardManager;
        this.coordinatorManager = coordinatorManager;
    }

    @FXML
    public void initialize() {
        RootState currentSystemState = applicationNavigationStore.getState();

        if (currentSystemState != null && currentSystemState.sessionState() != null) {
            User currentAuthenticatedUser = currentSystemState.sessionState().currentUserInSession();

            if (currentAuthenticatedUser != null) {
                systemUserNameLabel.setText(currentAuthenticatedUser.getName() + " " + currentAuthenticatedUser.getLastName());
                systemUserRoleLabel.setText("Rol: " + currentAuthenticatedUser.getRole());
                adjustUserInterfacePermissionsByRole(currentAuthenticatedUser.getRole());
            } else {
                Controller.showAlert("Sesión inválida", "No se detectó un usuario activo en el sistema. Por favor, inicie sesión nuevamente.", AlertType.WARNING);
            }
        }
    }

    private void adjustUserInterfacePermissionsByRole(String authenticatedUserRole) {
        hideAllNavigationButtonsByDefault();

        if (applicationDashboardManager.isAdministratorMenuAvailable(authenticatedUserRole)) {
            navigateToRegisterCoordinatorButton.setVisible(true);
            navigateToRegisterCoordinatorButton.setManaged(true);

            try {
                Coordinator validCurrentCoordinator = coordinatorManager.retrieveCurrentCoordinator();
                if (validCurrentCoordinator != null) {
                    navigateToRegisterCoordinatorButton.setText("Gestionar Coordinador");
                } else {
                    navigateToRegisterCoordinatorButton.setText("Registrar Coordinador");
                }
            } catch (ManagerException e) {
                navigateToRegisterCoordinatorButton.setText("Gestionar Coordinador");
            }

        } else if (applicationDashboardManager.isCoordinatorMenuAvailable(authenticatedUserRole)) {
            navigateToPractitionerManagementMenuButton.setVisible(true);
            navigateToPractitionerManagementMenuButton.setManaged(true);
            navigateToRegisterProfessorButton.setVisible(true);
            navigateToRegisterProfessorButton.setManaged(true);
            navigateToRegisterProjectButton.setVisible(true);
            navigateToRegisterProjectButton.setManaged(true);
            navigateToRegisterManagerButton.setVisible(true);
            navigateToRegisterManagerButton.setManaged(true);
            navigateToRegisterOrganizationButton.setVisible(true);
            navigateToRegisterOrganizationButton.setManaged(true);
            if (navigateToTemplatesButton != null) {
                navigateToTemplatesButton.setVisible(true);
                navigateToTemplatesButton.setManaged(true);
            }

            if (navigateToRegisterPeriodButton != null) {
                navigateToRegisterPeriodButton.setVisible(true);
                navigateToRegisterPeriodButton.setManaged(true);
            }
            if (navigateToRegisterPracticeGroupButton != null) {
                navigateToRegisterPracticeGroupButton.setVisible(true);
                navigateToRegisterPracticeGroupButton.setManaged(true);
            }

        } else if (applicationDashboardManager.isProfessorMenuAvailable(authenticatedUserRole)) {
            if (navigateToEvaluateReportsButton != null) {
                navigateToEvaluateReportsButton.setVisible(true);
                navigateToEvaluateReportsButton.setManaged(true);
            }
            if (navigateToGradePractitionerButton != null) {
                navigateToGradePractitionerButton.setVisible(true);
                navigateToGradePractitionerButton.setManaged(true);
            }
            if (navigateToReviewSelfEvaluationButton != null) {
                navigateToReviewSelfEvaluationButton.setVisible(true);
                navigateToReviewSelfEvaluationButton.setManaged(true);
            }

        } else if (applicationDashboardManager.isPractitionerMenuAvailable(authenticatedUserRole)) {
            navigateToPractitionerProjectsButton.setVisible(true);
            navigateToPractitionerProjectsButton.setManaged(true);

            if (navigateToLogbookButton != null) {
                navigateToLogbookButton.setVisible(true);
                navigateToLogbookButton.setManaged(true);
            }

            if (navigateToReportGeneratorButton != null) {
                navigateToReportGeneratorButton.setVisible(true);
                navigateToReportGeneratorButton.setManaged(true);
            }
            if (navigateToProgressReportButton != null) {
                navigateToProgressReportButton.setVisible(true);
                navigateToProgressReportButton.setManaged(true);
            }
            if (navigateToGradeViewButton != null) {
                navigateToGradeViewButton.setVisible(true);
                navigateToGradeViewButton.setManaged(true);
            }
            if (navigateToSelfEvaluationButton != null) {
                navigateToSelfEvaluationButton.setVisible(true);
                navigateToSelfEvaluationButton.setManaged(true);
            }
        }
    }

    private void hideAllNavigationButtonsByDefault() {
        if (navigateToRegisterCoordinatorButton != null) {
            navigateToRegisterCoordinatorButton.setVisible(false);
            navigateToRegisterCoordinatorButton.setManaged(false);
        }
        if (navigateToPractitionerManagementMenuButton != null) {
            navigateToPractitionerManagementMenuButton.setVisible(false);
            navigateToPractitionerManagementMenuButton.setManaged(false);
        }
        if (navigateToRegisterProfessorButton != null) {
            navigateToRegisterProfessorButton.setVisible(false);
            navigateToRegisterProfessorButton.setManaged(false);
        }
        if (navigateToRegisterProjectButton != null) {
            navigateToRegisterProjectButton.setVisible(false);
            navigateToRegisterProjectButton.setManaged(false);
        }
        if (navigateToRegisterManagerButton != null) {
            navigateToRegisterManagerButton.setVisible(false);
            navigateToRegisterManagerButton.setManaged(false);
        }
        if (navigateToRegisterOrganizationButton != null) {
            navigateToRegisterOrganizationButton.setVisible(false);
            navigateToRegisterOrganizationButton.setManaged(false);
        }
        if (navigateToPractitionerProjectsButton != null) {
            navigateToPractitionerProjectsButton.setVisible(false);
            navigateToPractitionerProjectsButton.setManaged(false);
        }
        if (navigateToRegisterPeriodButton != null) {
            navigateToRegisterPeriodButton.setVisible(false);
            navigateToRegisterPeriodButton.setManaged(false);
        }
        if (navigateToRegisterPracticeGroupButton != null) {
            navigateToRegisterPracticeGroupButton.setVisible(false);
            navigateToRegisterPracticeGroupButton.setManaged(false);
        }
        if (navigateToTemplatesButton != null) {
            navigateToTemplatesButton.setVisible(false);
            navigateToTemplatesButton.setManaged(false);
        }

        if (navigateToLogbookButton != null) {
            navigateToLogbookButton.setVisible(false);
            navigateToLogbookButton.setManaged(false);
        }

        if (navigateToReportGeneratorButton != null) {
            navigateToReportGeneratorButton.setVisible(false);
            navigateToReportGeneratorButton.setManaged(false);
        }

        if (navigateToEvaluateReportsButton != null) {
            navigateToEvaluateReportsButton.setVisible(false);
            navigateToEvaluateReportsButton.setManaged(false);
        }
        if (navigateToGradePractitionerButton != null) {
            navigateToGradePractitionerButton.setVisible(false);
            navigateToGradePractitionerButton.setManaged(false);
        }
        if (navigateToProgressReportButton != null) {
            navigateToProgressReportButton.setVisible(false);
            navigateToProgressReportButton.setManaged(false);
        }
        if (navigateToGradeViewButton != null) {
            navigateToGradeViewButton.setVisible(false);
            navigateToGradeViewButton.setManaged(false);
        }
        if (navigateToSelfEvaluationButton != null) {
            navigateToSelfEvaluationButton.setVisible(false);
            navigateToSelfEvaluationButton.setManaged(false);
        }
        if (navigateToReviewSelfEvaluationButton != null) {
            navigateToReviewSelfEvaluationButton.setVisible(false);
            navigateToReviewSelfEvaluationButton.setManaged(false);
        }
    }

    @FXML
    private void handleNavigateToRegisterCoordinatorAction(ActionEvent userActionEvent) {
        try {
            Coordinator validCurrentCoordinator = coordinatorManager.retrieveCurrentCoordinator();

            if (validCurrentCoordinator != null) {
                String targetCoordinatorIdentifier = String.valueOf(validCurrentCoordinator.getId());
                applicationNavigationStore.dispatch(new NavigationAction.ViewEntityDetails(AppSection.COORDINATOR_DETAILS, targetCoordinatorIdentifier));
            } else {
                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_COORDINATOR));
            }

        } catch (ManagerException e) {
            Controller.showAlert("Error de Servidor", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleNavigateToPractitionerManagementAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
    }

    @FXML
    private void handleNavigateToRegisterProfessorAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterProjectAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROJECT_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterManagerAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.MANAGER_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterOrganizationAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.ORGANIZATION_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToPractitionerProjectsAction(ActionEvent userActionEvent) {
        User currentAuthenticatedPractitioner = applicationNavigationStore.getState().sessionState().currentUserInSession();

        try {
            AppSection resolvedTargetNavigationSection = applicationDashboardManager.resolvePractitionerProjectsNavigation(currentAuthenticatedPractitioner.getId());
            applicationNavigationStore.dispatch(new NavigationAction.GoToSection(resolvedTargetNavigationSection));
        } catch (ManagerException e) {
            Controller.showAlert("Acceso denegado", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleNavigateToRegisterPeriodAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PERIOD));
    }

    @FXML
    private void handleNavigateToRegisterPracticeGroupAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTICE_GROUP));
    }

    @FXML
    private void handleNavigateToMessagesAction(ActionEvent userActionEvent) {
        try {
            applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.MESSAGES));
        } catch (Exception dispatchException) {
            Controller.showAlert("Error de Navegación", "No fue posible abrir la bandeja de mensajes.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleSystemLogoutAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }

    @FXML
    private void handleNavigateToLogbookAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_LOGBOOK));
    }

    @FXML
    private void handleNavigateToReportsList(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_REPORTS_LIST));
    }

    @FXML
    private void handleNavigateToEvaluateReportsAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_EVALUATE_REPORT));
    }

    @FXML
    private void handleNavigateToTemplatesAction(ActionEvent userActionEvent) {
        try {
            applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.TEMPLATE_GENERATOR));
        } catch (Exception dispatchException) {
            Controller.showAlert("Error de Navegación", "No fue posible abrir el generador de plantillas.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleNavigateToGradePractitionerAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.GRADE_PRACTITIONER));
    }

    @FXML
    private void handleNavigateToProgressReportAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROGRESS_REPORT_GENERATOR));
    }

    @FXML
    private void handleNavigateToGradeViewAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_GRADE_VIEW));
    }

    @FXML
    private void handleNavigateToSelfEvaluationAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PRACTITIONER_SELF_EVALUATION));
    }

    @FXML
    private void handleNavigateToReviewSelfEvaluationAction(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_REVIEW_SELF_EVALUATION));
    }
}
