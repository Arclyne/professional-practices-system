package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class CoordinatorPractitionerMenuController {

    @FXML
    private Button registerNewPractitionerButton;
    @FXML
    private Button assignProjectsButton;
    @FXML
    private Button returnToDashboardButton;

    private final Store applicationNavigationStore;

    @Inject
    public CoordinatorPractitionerMenuController(Store applicationNavigationStore) {
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    private void handleRegisterNewPractitionerAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTITIONER));
    }

    @FXML
    private void handleAssignProjectsAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PENDING_PRACTITIONER_SELECTION));
    }

    @FXML
    private void handleReturnToDashboardAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}