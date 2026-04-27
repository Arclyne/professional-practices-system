package mx.uv.fei.sandbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.manager.StartSessionManager;
import mx.uv.fei.presentation.RegisterActivityController;
import mx.uv.fei.presentation.RegisterProjectController;
import mx.uv.fei.presentation.StartSessionController;

public class GuiSandboxTwo extends Application {

    private IDatabaseConnection testDatabaseConnection;

    @Override
    public void start(Stage primaryApplicationStage) throws Exception {
        DatabasePropeties databaseProperties = new DatabasePropeties();
        testDatabaseConnection = new DataconnectionConfig(databaseProperties, "test").databaseConnection();
        TestDatabaseSetup.initialize(testDatabaseConnection);

        User testUserSession = new User();
        ProjectManager sharedProjectManager = new ProjectManager(testDatabaseConnection);
        StartSessionManager sharedSessionManager = new StartSessionManager(testDatabaseConnection, testUserSession);

        loadSandboxWindow(primaryApplicationStage, "/mx/uv/fei/presentation/StartSession.fxml",
                "Sandbox - Inicio de Sesión", sharedProjectManager, sharedSessionManager);

        Stage secondaryApplicationStage = new Stage();
        loadSandboxWindow(secondaryApplicationStage, "/mx/uv/fei/presentation/projectForm.fxml",
                "Sandbox - Registro de Proyecto", sharedProjectManager, sharedSessionManager);
    }

    private void loadSandboxWindow(Stage targetStage, String fxmlFilePath, String windowTitle,
            ProjectManager projectManager, StartSessionManager sessionManager) throws Exception {

        FXMLLoader userInterfaceLoader = new FXMLLoader(getClass().getResource(fxmlFilePath));

        userInterfaceLoader.setControllerFactory(controllerClassType -> {
            if (controllerClassType == StartSessionController.class) {
                return new StartSessionController(sessionManager);
            }
            if (controllerClassType == RegisterProjectController.class) {
                return new RegisterProjectController(projectManager);
            }
            if (controllerClassType == RegisterActivityController.class) {
                return new RegisterActivityController(projectManager);
            }

            try {
                return controllerClassType.getDeclaredConstructor().newInstance();
            } catch (Exception reflectionException) {
                throw new RuntimeException(
                        "Error instanciando controlador en Sandbox: " + controllerClassType.getName(),
                        reflectionException);
            }
        });

        Parent rootVisualNode = userInterfaceLoader.load();
        Scene applicationScene = new Scene(rootVisualNode);

        targetStage.setTitle(windowTitle);
        targetStage.setScene(applicationScene);
        targetStage.show();
    }

    public static void main(String[] applicationArguments) {
        launch(applicationArguments);
    }
}