package mx.uv.fei.presentation.shell;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
public class AdminShellController extends RoleShellController {

    private static final String HOME_VIEW = "/mx/uv/fei/presentation/adminHome.fxml";
    private static final String COORDINATION_VIEW = "/mx/uv/fei/presentation/coordination.fxml";
    private static final String MESSAGES_VIEW = "/mx/uv/fei/presentation/messageView.fxml";

    @FXML private Button homeNavButton;
    @FXML private Button coordinationNavButton;
    @FXML private Button messagesNavButton;

    @Inject
    public AdminShellController(AppStore store, DependencyInjector dependencyInjector) {
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
    private void handleShowCoordinationAction() {
        showSubView(COORDINATION_VIEW);
        selectNavButton(coordinationNavButton);
    }

    @FXML
    private void handleShowMessagesAction() {
        showSubView(MESSAGES_VIEW);
        selectNavButton(messagesNavButton);
    }

    @FXML
    private void handleLogoutAction() {
        logout();
    }
}
