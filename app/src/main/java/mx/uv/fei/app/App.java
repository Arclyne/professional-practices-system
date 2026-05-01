package mx.uv.fei.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import mx.uv.fei.appconfiguration.DatabaseProperties;
import mx.uv.fei.appconfiguration.DataconnectionConfig;
import mx.uv.fei.config.annotation.EtiquetteApplication;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import mx.uv.fei.dataacces.repositories.AdministratorDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.domain.statemachine.Store;

@StartEtiquette
@Profile("local")
public class App extends Application {
    private DependencyInjector dependencyInjector;

    @Override
    public void init() {
        this.dependencyInjector = EtiquetteApplication.run(this.getClass());
        dependencyInjector.retrieveInstance(DatabaseProperties.class);
        dependencyInjector.retrieveInstance(DataconnectionConfig.class);
        dependencyInjector.retrieveInstance(UserDAO.class);
        dependencyInjector.retrieveInstance(AdministratorDAO.class);
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