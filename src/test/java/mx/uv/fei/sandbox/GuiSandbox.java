package mx.uv.fei.sandbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiSandbox extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uv/fei/presentation/registerPractitioner.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Sandbox - Vista de Registro");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}