package mx.uv.fei.presentation.shell;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
public class ProfessorShellController extends RoleShellController {

    private static final String HOME_VIEW = "/mx/uv/fei/presentation/professorHome.fxml";
    private static final String MESSAGES_VIEW = "/mx/uv/fei/presentation/messageView.fxml";
    private static final String EVALUATE_REPORTS_VIEW = "/mx/uv/fei/presentation/professorEvaluateReport.fxml";
    private static final String GRADE_PRACTITIONERS_VIEW = "/mx/uv/fei/presentation/gradePractitioner.fxml";
    private static final String SELF_EVALUATIONS_VIEW = "/mx/uv/fei/presentation/reviewSelfEvaluation.fxml";

    @FXML private Button homeNavButton;
    @FXML private Button messagesNavButton;
    @FXML private Button evaluateReportsNavButton;
    @FXML private Button gradePractitionersNavButton;
    @FXML private Button selfEvaluationsNavButton;

    @Inject
    public ProfessorShellController(AppStore store, DependencyInjector dependencyInjector) {
        super(store, dependencyInjector);
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
    private void handleShowEvaluateReportsAction() {
        showSubView(EVALUATE_REPORTS_VIEW);
        selectNavButton(evaluateReportsNavButton);
    }

    @FXML
    private void handleShowGradePractitionersAction() {
        showSubView(GRADE_PRACTITIONERS_VIEW);
        selectNavButton(gradePractitionersNavButton);
    }

    @FXML
    private void handleShowSelfEvaluationsAction() {
        showSubView(SELF_EVALUATIONS_VIEW);
        selectNavButton(selfEvaluationsNavButton);
    }

    @FXML
    private void handleLogoutAction() {
        logout();
    }
}
