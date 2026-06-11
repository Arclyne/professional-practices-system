package mx.uv.fei.app;

import mx.uv.fei.config.annotation.core.EtiquetteApplication;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


@StartEtiquette
@Profile("local")
public class MainApplication extends Application {

    private static final String MAIN_VIEW_PATH = "/mx/uv/fei/presentation/main.fxml";
    private static final String WINDOW_TITLE = "Sistema de Gestión de Prácticas Profesionales - FEI";
    private static final double WINDOW_WIDTH = 450;
    private static final double WINDOW_HEIGHT = 600;

    private DependencyInjector dependencyInjector;

    @Override
    public void init() {
        dependencyInjector = EtiquetteApplication.run(this.getClass());
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Scene mainScene = buildMainScene();
            configureAndShowStage(primaryStage, mainScene);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Ocurrió un problema inesperado al iniciar el sistema. Por favor, intente más tarde.", e
            );
        }
    }

    private Scene buildMainScene() throws IOException {
        FXMLLoader viewLoader = new FXMLLoader(getClass().getResource(MAIN_VIEW_PATH));
        viewLoader.setControllerFactory(dependencyInjector::retrieveInstance);
        Parent rootNode = viewLoader.load();
        return new Scene(rootNode, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void configureAndShowStage(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.setTitle(WINDOW_TITLE);
        stage.show();
    }
}