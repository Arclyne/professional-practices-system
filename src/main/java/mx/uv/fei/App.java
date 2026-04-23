package mx.uv.fei;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("activityForms.fxml"));
        try {
            Parent root = loader.load();

            Scene scene = new Scene(root);

            stage.setTitle("Mi Primera Ventana con Scene Builder");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {

        }
    }

    @Override
    public void stop() throws Exception {
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}