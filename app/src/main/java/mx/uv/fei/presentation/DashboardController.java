package mx.uv.fei.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

@Component
public class DashboardController {

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private StackPane dashboardContent;

    @FXML private Button buttonRegisterCoordinator;
    @FXML private Button buttonRegisterPractitioner;
    @FXML private Button buttonRegisterProfessor;
    @FXML private Button buttonRegisterProject;
    @FXML private Button buttonRegisterActivity;

    private final Store store;

    @Inject
    public DashboardController(Store store) {
        this.store = store;
    }

    @FXML
    public void initialize() {
        RootState currentState = store.getState();
        User currentUser = currentState.sessionState().currentUserInSession();

        if (currentUser != null) {
            labelUserName.setText(currentUser.getName() + " " + currentUser.getLastName());
            labelUserRole.setText("Rol: " + currentUser.getRole());
            configurePermissions(currentUser.getRole());
        }

        buttonRegisterPractitioner.setOnAction(e -> navigateTo(AppSection.REGISTER_PRACTITIONER));
        buttonRegisterProfessor.setOnAction(e -> navigateTo(AppSection.REGISTER_PROFESSOR));
        buttonRegisterProject.setOnAction(e -> navigateTo(AppSection.REGISTER_PROJECT));
        buttonRegisterActivity.setOnAction(e -> navigateTo(AppSection.REGISTER_ACTIVITY));
        buttonRegisterCoordinator.setOnAction(e -> navigateTo(AppSection.REGISTER_COORDINATOR));
    }

    private void navigateTo(AppSection section) {
        store.dispatch(new NavigationAction.GoToSection(section));
    }

    private void configurePermissions(String role) {
        buttonRegisterCoordinator.setVisible(false);
        buttonRegisterCoordinator.setManaged(false);
        buttonRegisterPractitioner.setVisible(false);
        buttonRegisterPractitioner.setManaged(false);
        buttonRegisterProfessor.setVisible(false);
        buttonRegisterProfessor.setManaged(false);
        buttonRegisterProject.setVisible(false);
        buttonRegisterProject.setManaged(false);
        buttonRegisterActivity.setVisible(false);
        buttonRegisterActivity.setManaged(false);

        if ("Administrador".equalsIgnoreCase(role)) {
            buttonRegisterCoordinator.setVisible(true);
            buttonRegisterCoordinator.setManaged(true);
            buttonRegisterPractitioner.setVisible(true);
            buttonRegisterPractitioner.setManaged(true);
            buttonRegisterProfessor.setVisible(true);
            buttonRegisterProfessor.setManaged(true);
            buttonRegisterProject.setVisible(true);
            buttonRegisterProject.setManaged(true);
            buttonRegisterActivity.setVisible(true);
            buttonRegisterActivity.setManaged(true);

        } else if ("Coordinador".equalsIgnoreCase(role)) {
            buttonRegisterPractitioner.setVisible(true);
            buttonRegisterPractitioner.setManaged(true);
            buttonRegisterProfessor.setVisible(true);
            buttonRegisterProfessor.setManaged(true);
            buttonRegisterProject.setVisible(true);
            buttonRegisterProject.setManaged(true);

        } else if ("Profesor".equalsIgnoreCase(role)) {
            buttonRegisterActivity.setVisible(true);
            buttonRegisterActivity.setManaged(true);
        }
    }

    @FXML
    private void handleLogout() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }
}