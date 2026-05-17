package mx.uv.fei.presentation;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import java.net.URL;

import javafx.stage.Stage;
import javafx.util.Duration;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.AdminManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

@Component
public class MainController {

    @FXML
    private StackPane contentArea;

    private Runnable unsubscribe;
    private final DependencyInjector dependencyInjector;
    private final Store store;
    private final AdminManager administratorManager;

    private AppSection activeApplicationSection = null;

    @Inject
    public MainController(DependencyInjector dependencyInjector, Store applicationNavigationStore, AdminManager administratorManager) {
        this.dependencyInjector = dependencyInjector;
        this.store = applicationNavigationStore;
        this.administratorManager = administratorManager;
    }

    @FXML
    public void initialize() {
        this.unsubscribe = store.subscribe(this::handleApplicationStateChange);
        startAppFlow();
    }

    private Stage getStage() {
        return (Stage) contentArea.getScene().getWindow();
    }

    private void handleApplicationStateChange(RootState previousState, RootState newState) {
        AppSection targetSection = newState.navigationState().currentSection();

        Platform.runLater(() -> {
            if (this.activeApplicationSection != targetSection) {
                this.activeApplicationSection = targetSection;

                Stage stage = getStage();

                if (targetSection == AppSection.DASHBOARD) {
                    stage.setWidth(1100);
                    stage.setHeight(750);
                    stage.centerOnScreen();
                } else if (targetSection == AppSection.LOGIN || targetSection == AppSection.ADMIN_REGISTRATION) {
                    stage.setWidth(500);
                    stage.setHeight(450);
                    stage.centerOnScreen();
                }

                switch (targetSection) {
                    case SPLASH_SCREEN -> loadView("/mx/uv/fei/presentation/loading.fxml");
                    case ADMIN_REGISTRATION -> loadView("/mx/uv/fei/presentation/registerAdmin.fxml");
                    case LOGIN -> loadView("/mx/uv/fei/presentation/startSession.fxml");
                    case DASHBOARD -> loadView("/mx/uv/fei/presentation/dashboard.fxml");
                    case PASSWORD_RESET -> loadView("/mx/uv/fei/presentation/passwordReset.fxml");
                    case TOKEN_VERIFICATION -> loadView("/mx/uv/fei/presentation/tokenVerification.fxml");
                    case REGISTER_PRACTITIONER -> loadView("/mx/uv/fei/presentation/registerPractitioner.fxml");
                    case REGISTER_PROFESSOR -> loadView("/mx/uv/fei/presentation/registerProfessor.fxml");
                    case REGISTER_ACTIVITY -> loadView("/mx/uv/fei/presentation/activityForms.fxml");
                    case REGISTER_PROJECT -> loadView("/mx/uv/fei/presentation/projectForm.fxml");
                    case REGISTER_COORDINATOR -> loadView("/mx/uv/fei/presentation/registerCoordinator.fxml");
                    case COORDINATOR_DETAILS -> loadView("/mx/uv/fei/presentation/coordinatorDetails.fxml");
                    case PRIORITIZE_PROJECTS -> loadView("/mx/uv/fei/presentation/prioritizeProjects.fxml");
                    case VIEW_PRACTITIONER_PRIORITIES -> loadView("/mx/uv/fei/presentation/viewPractitionerPriorities.fxml");
                    case COORDINATOR_PRACTITIONER_MENU -> loadView("/mx/uv/fei/presentation/coordinatorPractitionerMenu.fxml");
                    case PENDING_PRACTITIONER_SELECTION -> loadView("/mx/uv/fei/presentation/pendingPractitionerSelection.fxml");
                    case REGISTER_MANAGER -> loadView("/mx/uv/fei/presentation/registerManager.fxml");
                    case PROFESSOR_MANAGEMENT_MENU -> loadView("/mx/uv/fei/presentation/managerProfessor.fxml");
                    case ASSIGN_PROJECT -> loadView("/mx/uv/fei/presentation/assignProject.fxml");
                    case PROJECT_MANAGEMENT_MENU -> loadView("/mx/uv/fei/presentation/manageProjects.fxml");
                    case MANAGER_MANAGEMENT_MENU -> loadView("/mx/uv/fei/presentation/manageManagers.fxml");
                    default -> Controller.showAlert("Error de Navegación", "No se encontró la ruta para la sección solicitada en el sistema.", AlertType.ERROR);
                }
            }
        });
    }

    private void loadView(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new RuntimeException("Ruta FXML no encontrada: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            loader.setControllerFactory(dependencyInjector::retrieveInstance);

            Parent view = loader.load();

            if (view instanceof Region regionView) {
                regionView.prefWidthProperty().bind(contentArea.widthProperty());
                regionView.prefHeightProperty().bind(contentArea.heightProperty());
            }

            contentArea.getChildren().setAll(view);

        } catch (Exception interfaceLoadException) {
            Controller.showAlert("Error de Interfaz",
                    "No se pudo cargar la pantalla: " + fxmlPath + "\nDetalle: " + interfaceLoadException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void startAppFlow() {

        store.dispatch(new NavigationAction.GoToSection(AppSection.SPLASH_SCREEN));

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        pause.setOnFinished(pauseTransitionEvent -> {
            try {
                boolean adminExists = administratorManager.checkSystemHasAdmin();

                if (!adminExists) {
                    store.dispatch(new NavigationAction.GoToSection(AppSection.ADMIN_REGISTRATION));
                } else {
                    store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
                }

            } catch (ManagerException startupException) {
                Controller.showAlert("Error de Arranque",
                        "El sistema falló al iniciar: " + startupException.getMessage(),
                        AlertType.ERROR);
            }
        });

        pause.play();
    }

    public void dispose() {
        if (unsubscribe != null) {
            unsubscribe.run();
        }
    }
}