package mx.uv.fei.sandbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.manager.RegisterProfessorManager;
import mx.uv.fei.presentation.RegisterProfessorController;

public class GuiProfessor extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabasePropeties propeties = new DatabasePropeties();
        IDatabaseConnection dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);
        RegisterProfessorManager manager = new RegisterProfessorManager(dbConnection);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uv/fei/presentation/registerProfessor.fxml"));
        Scene scene = new Scene(loader.load());

        RegisterProfessorController controller = loader.getController();
        controller.setManager(manager);

        stage.setTitle("Sandbox - Vista de Registro");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}