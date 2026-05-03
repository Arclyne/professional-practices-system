package mx.uv.fei.presentation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import java.net.URL;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.manager.AdminManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

import static java.lang.Thread.sleep;

@Component
public class MainController {

    @FXML
    private StackPane contentArea;

    private Runnable unsubscribe;
    private final DependencyInjector injector;
    private final Store store;
    private final AdminManager registerAdminManager;

    private AppSection activeSection = null;

    @Inject
    public MainController(DependencyInjector injector, Store store, AdminManager registerAdminManager) {
        this.injector = injector;
        this.store = store;
        this.registerAdminManager = registerAdminManager;
    }

    @FXML
    public void initialize() {
        this.unsubscribe = store.subscribe(this::handleStateChange);
        startAppFlow();
    }

    private void handleStateChange(RootState prevState, RootState newState) {
        AppSection targetSection = newState.navigationState().currentSection();

        Platform.runLater(() -> {
            if (this.activeSection != targetSection) {
                this.activeSection = targetSection;

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
                    default -> System.err.println(">>> ALERTA: No hay un case para " + targetSection);
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

            loader.setControllerFactory(injector::retrieveInstance);

            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            System.err.println("¡ERROR FATAL AL CARGAR LA VISTA [" + fxmlPath + "]!");
            e.printStackTrace();
            CommonControler.showAlert("Error de Interfaz",
                    "No se pudo cargar la pantalla: " + fxmlPath + "\nDetalle: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void startAppFlow() {
        try {
            boolean adminExists = registerAdminManager.checkSystemHasAdmin();

            sleep(2000);

            if (!adminExists) {
                store.dispatch(new NavigationAction.GoToSection(AppSection.ADMIN_REGISTRATION));
            } else {
                store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
            }

        } catch (Exception e) {
            CommonControler.showAlert("Error de Arranque",
                    "El sistema falló al iniciar: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    public void dispose() {
        if (unsubscribe != null) {
            unsubscribe.run();
        }
    }
}