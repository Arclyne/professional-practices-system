package mx.uv.fei.presentation.shell;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
public class CoordinatorShellController extends RoleShellController {

    private static final String BASE_PATH = "/mx/uv/fei/presentation/";
    private static final String HOME_VIEW = BASE_PATH + "coordinatorHome.fxml";
    private static final String MESSAGES_VIEW = BASE_PATH + "messageView.fxml";
    private static final String PRACTITIONERS_VIEW = BASE_PATH + "coordinatorPractitioners.fxml";
    private static final String ASSIGNMENTS_VIEW = BASE_PATH + "pendingPractitionerSelection.fxml";
    private static final String PROJECTS_VIEW = BASE_PATH + "coordinatorProjects.fxml";
    private static final String ORGANIZATIONS_VIEW = BASE_PATH + "coordinatorOrganizations.fxml";
    private static final String PROFESSORS_VIEW = BASE_PATH + "coordinatorProfessors.fxml";
    private static final String MANAGERS_VIEW = BASE_PATH + "coordinatorManagers.fxml";
    private static final String PERIODS_VIEW = BASE_PATH + "coordinatorPeriods.fxml";
    private static final String GROUPS_VIEW = BASE_PATH + "coordinatorGroups.fxml";
    private static final String TEMPLATES_VIEW = BASE_PATH + "templateGenerator.fxml";

    @FXML private Button homeNavButton;
    @FXML private Button messagesNavButton;
    @FXML private Button practitionersNavButton;
    @FXML private Button assignmentsNavButton;
    @FXML private Button projectsNavButton;
    @FXML private Button organizationsNavButton;
    @FXML private Button professorsNavButton;
    @FXML private Button managersNavButton;
    @FXML private Button periodsNavButton;
    @FXML private Button groupsNavButton;
    @FXML private Button templatesNavButton;

    @Inject
    public CoordinatorShellController(AppStore store, DependencyInjector dependencyInjector, ShellNavigator shellNavigator) {
        super(store, dependencyInjector, shellNavigator);
    }

    @FXML
    public void initialize() {
        initializeShell();
        showSubView(HOME_VIEW);
        selectNavButton(homeNavButton);
    }

    @FXML
    private void handleShowHomeAction() {
        showSubView(HOME_VIEW);
        selectNavButton(homeNavButton);
    }

    @FXML
    private void handleShowMessagesAction() {
        showSubView(MESSAGES_VIEW);
        selectNavButton(messagesNavButton);
    }

    @FXML
    private void handleShowPractitionersAction() {
        showSubView(PRACTITIONERS_VIEW);
        selectNavButton(practitionersNavButton);
    }

    @FXML
    private void handleShowAssignmentsAction() {
        showSubView(ASSIGNMENTS_VIEW);
        selectNavButton(assignmentsNavButton);
    }

    @FXML
    private void handleShowProjectsAction() {
        showSubView(PROJECTS_VIEW);
        selectNavButton(projectsNavButton);
    }

    @FXML
    private void handleShowOrganizationsAction() {
        showSubView(ORGANIZATIONS_VIEW);
        selectNavButton(organizationsNavButton);
    }

    @FXML
    private void handleShowProfessorsAction() {
        showSubView(PROFESSORS_VIEW);
        selectNavButton(professorsNavButton);
    }

    @FXML
    private void handleShowManagersAction() {
        showSubView(MANAGERS_VIEW);
        selectNavButton(managersNavButton);
    }

    @FXML
    private void handleShowPeriodsAction() {
        showSubView(PERIODS_VIEW);
        selectNavButton(periodsNavButton);
    }

    @FXML
    private void handleShowGroupsAction() {
        showSubView(GROUPS_VIEW);
        selectNavButton(groupsNavButton);
    }

    @FXML
    private void handleShowTemplatesAction() {
        showSubView(TEMPLATES_VIEW);
        selectNavButton(templatesNavButton);
    }

    @FXML
    private void handleLogoutAction() {
        logout();
    }
}
