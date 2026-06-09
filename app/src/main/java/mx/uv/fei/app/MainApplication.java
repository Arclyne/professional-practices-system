package mx.uv.fei.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import mx.uv.fei.config.annotation.core.EtiquetteApplication;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;

import java.io.IOException;

@StartEtiquette
@Profile("local")

public class MainApplication extends Application {

    private DependencyInjector systemDependencyInjector;
    @Override
    public void init() {
        this.systemDependencyInjector = EtiquetteApplication.run(this.getClass());
    }

    @Override
    public void start(Stage primaryApplicationStage) {
        FXMLLoader graphicalUserInterfaceLoader = new FXMLLoader(getClass().getResource("/mx/uv/fei/presentation/main.fxml"));
        graphicalUserInterfaceLoader.setControllerFactory(systemDependencyInjector::retrieveInstance);

        try {
            Parent rootWindowNode = graphicalUserInterfaceLoader.load();
            Scene mainApplicationScene = new Scene(rootWindowNode, 450, 600);

            primaryApplicationStage.setScene(mainApplicationScene);
            primaryApplicationStage.setTitle("Sistema de Gestion de Practicas Profesionales - FEI");
            primaryApplicationStage.show();

        } catch (IOException e) {
            throw new IllegalStateException("Ocurrio un problema inesperado al iniciar el sistema. Por favor, intente mas tarde.", e);
        }
    }
}
