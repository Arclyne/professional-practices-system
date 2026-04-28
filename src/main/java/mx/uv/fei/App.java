package mx.uv.fei;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.config.DependencyInjector;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class App extends Application {

    @Override
    public void start(Stage primaryApplicationStage) throws Exception {
        DatabasePropeties databaseProperties = new DatabasePropeties();
        IDatabaseConnection databaseConnection = new DataconnectionConfig(databaseProperties, "local")
                .databaseConnection();
        DependencyInjector injector = new DependencyInjector(databaseConnection);
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/mx/uv/fei/presentation/main.fxml"));

        mainLoader.setControllerFactory(controllerClass -> injector.getController(controllerClass));

        Parent rootNode = mainLoader.load();

        Scene mainScene = new Scene(rootNode, 1024, 768);

        primaryApplicationStage.setScene(mainScene);
        primaryApplicationStage.setTitle("Sistema de Gestión de Prácticas Profesionales - FEI");
        primaryApplicationStage.setMinWidth(800);
        primaryApplicationStage.setMinHeight(600);
        primaryApplicationStage.show();
    }

    @Override
    public void stop() throws Exception {
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] applicationArguments) {
        launch(applicationArguments);
    }
}