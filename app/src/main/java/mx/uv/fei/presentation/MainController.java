package mx.uv.fei.presentation;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import java.net.URL;

import javafx.util.Duration;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.manager.AdminManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

@Component
public class MainController {

    @FXML
    private StackPane contentArea;

    private Runnable stateChangeSubscription;
    private final DependencyInjector dependencyInjector;
    private final Store applicationNavigationStore;
    private final AdminManager administratorManager;

    private AppSection activeApplicationSection = null;

    @Inject
    public MainController(DependencyInjector dependencyInjector, Store applicationNavigationStore, AdminManager administratorManager) {
        this.dependencyInjector = dependencyInjector;
        this.applicationNavigationStore = applicationNavigationStore;
        this.administratorManager = administratorManager;
    }

    @FXML
    public void initialize() {
        this.stateChangeSubscription = applicationNavigationStore.subscribe(this::handleApplicationStateChange);
        startApplicationFlow();
    }

    private void handleApplicationStateChange(RootState previousState, RootState newState) {
        AppSection targetApplicationSection = newState.navigationState().currentSection();

        Platform.runLater(() -> {
            if (this.activeApplicationSection != targetApplicationSection) {
                this.activeApplicationSection = targetApplicationSection;

                switch (targetApplicationSection) {
                    case SPLASH_SCREEN -> loadInterfaceView("/mx/uv/fei/presentation/loading.fxml");
                    case ADMIN_REGISTRATION -> loadInterfaceView("/mx/uv/fei/presentation/registerAdmin.fxml");
                    case LOGIN -> loadInterfaceView("/mx/uv/fei/presentation/startSession.fxml");
                    case DASHBOARD -> loadInterfaceView("/mx/uv/fei/presentation/dashboard.fxml");
                    case PASSWORD_RESET -> loadInterfaceView("/mx/uv/fei/presentation/passwordReset.fxml");
                    case TOKEN_VERIFICATION -> loadInterfaceView("/mx/uv/fei/presentation/tokenVerification.fxml");
                    case REGISTER_PRACTITIONER -> loadInterfaceView("/mx/uv/fei/presentation/registerPractitioner.fxml");
                    case REGISTER_PROFESSOR -> loadInterfaceView("/mx/uv/fei/presentation/registerProfessor.fxml");
                    case REGISTER_ACTIVITY -> loadInterfaceView("/mx/uv/fei/presentation/activityForms.fxml");
                    case REGISTER_PROJECT -> loadInterfaceView("/mx/uv/fei/presentation/projectForm.fxml");
                    case REGISTER_COORDINATOR -> loadInterfaceView("/mx/uv/fei/presentation/registerCoordinator.fxml");
                    case PRIORITIZE_PROJECTS -> loadInterfaceView("/mx/uv/fei/presentation/prioritizeProjects.fxml");
                    case VIEW_PRACTITIONER_PRIORITIES -> loadInterfaceView("/mx/uv/fei/presentation/viewPractitionerPriorities.fxml");
                    case COORDINATOR_PRACTITIONER_MENU -> loadInterfaceView("/mx/uv/fei/presentation/coordinatorPractitionerMenu.fxml");
                    case PENDING_PRACTITIONER_SELECTION -> loadInterfaceView("/mx/uv/fei/presentation/pendingPractitionerSelection.fxml");
                    case ASSIGN_PROJECT -> loadInterfaceView("/mx/uv/fei/presentation/assignProject.fxml");
                    default -> Controller.showAlert("Error de Navegación", "No se encontró la ruta para la sección solicitada en el sistema.", AlertType.ERROR);
                }
            }
        });
    }

    private void loadInterfaceView(String fxmlFilePath) {
        try {
            URL fxmlFileUrl = getClass().getResource(fxmlFilePath);

            if (fxmlFileUrl == null) {
                throw new RuntimeException("Ruta FXML no encontrada en el sistema: " + fxmlFilePath);
            }

            FXMLLoader viewLoader = new FXMLLoader(fxmlFileUrl);
            viewLoader.setControllerFactory(dependencyInjector::retrieveInstance);
            Parent loadedView = viewLoader.load();
            contentArea.getChildren().setAll(loadedView);

        } catch (Exception loadingException) {
            Controller.showAlert("Error de Interfaz",
                    "No fue posible cargar la pantalla solicitada. Por favor, intente de nuevo o contacte al soporte técnico.",
                    AlertType.ERROR);
        }
    }

    private void startApplicationFlow() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.SPLASH_SCREEN));

        PauseTransition initializationPause = new PauseTransition(Duration.seconds(2));

        initializationPause.setOnFinished(userActionEvent -> {
            try {
                boolean systemHasAdministrator = administratorManager.checkSystemHasAdmin();

                if (!systemHasAdministrator) {
                    applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.ADMIN_REGISTRATION));
                } else {
                    applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
                }

            } catch (Exception initializationException) {
                Controller.showAlert("Error de Arranque",
                        "El sistema falló al iniciar sus componentes. Verifique su conexión a la base de datos.",
                        AlertType.ERROR);
            }
        });

        initializationPause.play();
    }

    public void dispose() {
        if (stateChangeSubscription != null) {
            stateChangeSubscription.run();
        }
    }
}