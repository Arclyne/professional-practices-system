package mx.uv.fei.presentation.shell;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
public class PractitionerShellController extends RoleShellController {

    private static final String HOME_VIEW = "/mx/uv/fei/presentation/practitionerHome.fxml";
    private static final String POSTULATIONS_VIEW = "/mx/uv/fei/presentation/prioritizeProjects.fxml";
    private static final String PROJECT_VIEW = "/mx/uv/fei/presentation/viewAssignedProject.fxml";
    private static final String MESSAGES_VIEW = "/mx/uv/fei/presentation/messageView.fxml";
    private static final String LOGBOOK_VIEW = "/mx/uv/fei/presentation/practitionerLogbook.fxml";
    private static final String REPORTS_VIEW = "/mx/uv/fei/presentation/practitionerReportsList.fxml";
    private static final String DOCUMENTS_VIEW = "/mx/uv/fei/presentation/practitionerDocuments.fxml";
    private static final String SELF_EVALUATION_VIEW = "/mx/uv/fei/presentation/practitionerSelfEvaluation.fxml";
    private static final String GRADE_VIEW = "/mx/uv/fei/presentation/practitionerGradeView.fxml";

    @FXML private Button homeNavButton;
    @FXML private Button postulationsNavButton;
    @FXML private Button projectNavButton;
    @FXML private Button messagesNavButton;
    @FXML private Button logbookNavButton;
    @FXML private Button reportsNavButton;
    @FXML private Button documentsNavButton;
    @FXML private Button selfEvaluationNavButton;
    @FXML private Button gradeNavButton;

    @Inject
    public PractitionerShellController(AppStore store, DependencyInjector dependencyInjector, ShellNavigator shellNavigator) {
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
    private void handleShowPostulationsAction() {
        showSubView(POSTULATIONS_VIEW);
        selectNavButton(postulationsNavButton);
    }

    @FXML
    private void handleShowProjectAction() {
        showSubView(PROJECT_VIEW);
        selectNavButton(projectNavButton);
    }

    @FXML
    private void handleShowMessagesAction() {
        showSubView(MESSAGES_VIEW);
        selectNavButton(messagesNavButton);
    }

    @FXML
    private void handleShowLogbookAction() {
        showSubView(LOGBOOK_VIEW);
        selectNavButton(logbookNavButton);
    }

    @FXML
    private void handleShowReportsAction() {
        showSubView(REPORTS_VIEW);
        selectNavButton(reportsNavButton);
    }

    @FXML
    private void handleShowDocumentsAction() {
        showSubView(DOCUMENTS_VIEW);
        selectNavButton(documentsNavButton);
    }

    @FXML
    private void handleShowSelfEvaluationAction() {
        showSubView(SELF_EVALUATION_VIEW);
        selectNavButton(selfEvaluationNavButton);
    }

    @FXML
    private void handleShowGradeAction() {
        showSubView(GRADE_VIEW);
        selectNavButton(gradeNavButton);
    }

    @FXML
    private void handleLogoutAction() {
        logout();
    }
}
