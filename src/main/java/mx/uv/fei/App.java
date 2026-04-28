package mx.uv.fei;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import mx.uv.fei.domain.manager.SceneManager;

public class App extends Application {

    @Override
    public void start(Stage primaryApplicationStage) throws Exception {
        SceneManager applicationSceneManager = new SceneManager(primaryApplicationStage);
        applicationSceneManager.changeApplicationScene("/mx/uv/fei/presentation/PasswordReset.fxml",
                "Iniciar Sesión");
    }

    @Override
    public void stop() throws Exception {
        Platform.exit();
    }

    public static void main(String[] applicationArguments) {
        launch(applicationArguments);
    }
}