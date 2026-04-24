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
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.presentation.RegisterActivityController;
import mx.uv.fei.presentation.RegisterProjectController;

public class GuiSandboxTwo extends Application {

    private IDatabaseConnection databaseConnection;
    private DatabasePropeties propeties;

    @Override
    public void start(Stage primaryApplicationStage) throws Exception {
        propeties = new DatabasePropeties();
        databaseConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(databaseConnection);

        ProjectManager sharedProjectManager = new ProjectManager(databaseConnection);

        FXMLLoader activityInterfaceLoader = new FXMLLoader(
                getClass().getResource("/mx/uv/fei/presentation/ActivityForm.fxml"));

        activityInterfaceLoader.setControllerFactory(controllerClassType -> {
            if (controllerClassType == RegisterActivityController.class) {
                return new RegisterActivityController(sharedProjectManager);
            }
            try {
                return controllerClassType.getDeclaredConstructor().newInstance();
            } catch (Exception reflectionException) {
                throw new RuntimeException(reflectionException);
            }
        });

        Parent activityRootNode = activityInterfaceLoader.load();
        Scene activityApplicationScene = new Scene(activityRootNode);

        primaryApplicationStage.setTitle("Sandbox - Registro de Actividad");
        primaryApplicationStage.setScene(activityApplicationScene);
        primaryApplicationStage.show();

        Stage secondaryApplicationStage = new Stage();
        FXMLLoader projectInterfaceLoader = new FXMLLoader(
                getClass().getResource("/mx/uv/fei/presentation/ProjectForm.fxml"));

        projectInterfaceLoader.setControllerFactory(controllerClassType -> {
            if (controllerClassType == RegisterProjectController.class) {
                return new RegisterProjectController(sharedProjectManager);
            }
            try {
                return controllerClassType.getDeclaredConstructor().newInstance();
            } catch (Exception reflectionException) {
                throw new RuntimeException(reflectionException);
            }
        });

        Parent projectRootNode = projectInterfaceLoader.load();
        Scene projectApplicationScene = new Scene(projectRootNode);

        secondaryApplicationStage.setTitle("Sandbox - Registro de Proyecto");
        secondaryApplicationStage.setScene(projectApplicationScene);
        secondaryApplicationStage.show();
    }

    public static void main(String[] applicationArguments) {
        launch(applicationArguments);
    }
}