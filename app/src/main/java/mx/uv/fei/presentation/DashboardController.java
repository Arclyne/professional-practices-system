package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.domain.manager.DashboardManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

@Component
public class DashboardController {

    @FXML
    private Label systemUserNameLabel;
    @FXML
    private Label systemUserRoleLabel;
    @FXML
    private StackPane dashboardMainContentArea;

    @FXML
    private Button navigateToRegisterCoordinatorButton;
    @FXML
    private Button navigateToPractitionerManagementMenuButton;
    @FXML
    private Button navigateToRegisterProfessorButton;
    @FXML
    private Button navigateToRegisterProjectButton;
    @FXML
    private Button navigateToRegisterManagerButton;
    @FXML
    private Button navigateToRegisterActivityButton;
    @FXML
    private Button navigateToPractitionerProjectsButton;
    @FXML
    private Button systemLogoutButton;

    private final Store applicationNavigationStore;
    private final DashboardManager applicationDashboardManager;
    private final CoordinatorManager coordinatorManager;

    @Inject
    public DashboardController(Store applicationNavigationStore, DashboardManager applicationDashboardManager, CoordinatorManager coordinatorManager) {
        this.applicationNavigationStore = applicationNavigationStore;
        this.applicationDashboardManager = applicationDashboardManager;
        this.coordinatorManager = coordinatorManager;
    }

    @FXML
    public void initialize() {
        try {
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
        } catch (Exception initializationException) {
            Controller.showAlert("Error del Sistema", "No se pudo inicializar el panel de control.", AlertType.ERROR);
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
            } catch (ManagerException databaseQueryException) {
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

        } else if (applicationDashboardManager.isProfessorMenuAvailable(authenticatedUserRole)) {
            navigateToRegisterActivityButton.setVisible(true);
            navigateToRegisterActivityButton.setManaged(true);

        } else if (applicationDashboardManager.isPractitionerMenuAvailable(authenticatedUserRole)) {
            navigateToPractitionerProjectsButton.setVisible(true);
            navigateToPractitionerProjectsButton.setManaged(true);
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
        if (navigateToRegisterActivityButton != null) {
            navigateToRegisterActivityButton.setVisible(false);
            navigateToRegisterActivityButton.setManaged(false);
        }
        if (navigateToPractitionerProjectsButton != null) {
            navigateToPractitionerProjectsButton.setVisible(false);
            navigateToPractitionerProjectsButton.setManaged(false);
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

        } catch (ManagerException managerRetrievalException) {
            Controller.showAlert("Error de Servidor", managerRetrievalException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleNavigateToPractitionerManagementAction(ActionEvent userActionEvent) {
        try {
            applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
        } catch (Exception dispatchException) {
            Controller.showAlert("Error de Navegación", "No fue posible abrir el menú de gestión de practicantes.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleNavigateToRegisterProfessorAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROFESSOR_MANAGEMENT_MENU));

    }

    @FXML
    private void handleNavigateToRegisterProjectAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PROJECT_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterManagerAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.MANAGER_MANAGEMENT_MENU));
    }

    @FXML
    private void handleNavigateToRegisterActivityAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_ACTIVITY));
    }

    @FXML
    private void handleNavigateToPractitionerProjectsAction(ActionEvent userActionEvent) {
        User currentAuthenticatedPractitioner = applicationNavigationStore.getState().sessionState().currentUserInSession();

        try {
            AppSection resolvedTargetNavigationSection = applicationDashboardManager.resolvePractitionerProjectsNavigation(currentAuthenticatedPractitioner.getId());
            applicationNavigationStore.dispatch(new NavigationAction.GoToSection(resolvedTargetNavigationSection));
        } catch (ManagerException verificationException) {
            Controller.showAlert("Acceso denegado", verificationException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleSystemLogoutAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }
}