package mx.uv.fei;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        Label label = new Label("SPP - UV \nRunning on Java: " + javaVersion + "\nJavaFX: " + javafxVersion);

        String welcomeMessage = """
                Welcome to the Professional Practices System.
                Faculty of Statistics and Informatics (FEI).
                """;
        System.out.println(welcomeMessage);

        Scene scene = new Scene(new StackPane(label), 400, 200);
        stage.setTitle("Java Environment Test"); 
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}