package mx.uv.fei.presentation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import mx.uv.fei.config.DependencyInjector;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.AuthAction;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

public class MainController {

    @FXML
    private StackPane contentArea;

    private Runnable unsubscribe;

    private final DependencyInjector injector;

    public MainController(DependencyInjector injector) {
        this.injector = injector;
    }

    @FXML
    public void initialize() {
        this.unsubscribe = Store.getInstance().subscribe(this::handleStateChange);
        startAppFlow();
    }

    private void handleStateChange(RootState prevState, RootState newState) {
        Platform.runLater(() -> {
            AppSection prevSection = prevState.navigationState().currentSection();
            AppSection currentSection = newState.navigationState().currentSection();

            if (prevSection != currentSection) {
                switch (currentSection) {
                    case SPLASH_SCREEN -> loadView("/mx/uv/fei/presentation/loading.fxml");
                    case ADMIN_REGISTRATION -> loadView("/mx/uv/fei/presentation/RegisterAdmin.fxml");
                    case LOGIN -> loadView("/mx/uv/fei/presentation/login.fxml");
                    case DASHBOARD -> loadView("/mx/uv/fei/presentation/dashboard.fxml");
                }
            }
        });
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            loader.setControllerFactory(injector::getController);

            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("Error crítico al cargar la vista [" + fxmlPath + "]: " + e.getMessage());
        }
    }

    private void startAppFlow() {
        Store store = Store.getInstance();
        store.dispatch(new NavigationAction.GoToSection(AppSection.SPLASH_SCREEN));

        Thread bootThread = new Thread(() -> {
            try {
                Thread.sleep(1000);

                boolean adminExists = injector.getRegisterAdminManager().checkSystemHasAdmin();

                store.dispatch(new AuthAction.AdminDetectionResult(adminExists));

                if (!adminExists) {
                    store.dispatch(new NavigationAction.GoToSection(AppSection.ADMIN_REGISTRATION));
                } else {
                    store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ManagerException e) {
                Platform.runLater(() -> {
                    CommonControler.showAlert("Falla de Conexión",
                            "No se pudo conectar a la base de datos de la facultad.\nDetalle: " + e.getMessage(),
                            AlertType.ERROR);
                });
            }
        });

        bootThread.setDaemon(true);
        bootThread.start();
    }

    public void dispose() {
        if (unsubscribe != null) {
            unsubscribe.run();
        }
    }
}