package mx.uv.fei.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import mx.uv.fei.config.annotation.EtiquetteApplication;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;


@StartEtiquette
@Profile("local")
public class App extends Application {
    private DependencyInjector dependencyInjector;

    @Override
    public void init() {
        this.dependencyInjector = EtiquetteApplication.run(this.getClass());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uv/fei/presentation/main.fxml"));
        loader.setControllerFactory(dependencyInjector::retrieveInstance);

        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.setTitle("Sistema de Gestión de Prácticas Profesionales - FEI");
        primaryStage.show();
    }
}