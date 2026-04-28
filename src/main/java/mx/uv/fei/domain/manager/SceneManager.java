package mx.uv.fei.domain.manager;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.User;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class SceneManager {

    private final IDatabaseConnection databaseConnection;
    private final Stage applicationPrimaryStage;
    private final User userInSession;

    public SceneManager(Stage applicationPrimaryStage) {
        this.applicationPrimaryStage = applicationPrimaryStage;

        DatabasePropeties databaseProperties = new DatabasePropeties();
        this.databaseConnection = new DataconnectionConfig(databaseProperties, "local").databaseConnection();
        this.userInSession = new User();
        this.applicationPrimaryStage.setUserData(this);
    }

    public void changeApplicationScene(String fxmlFilePath, String windowTitle) throws IOException {
        FXMLLoader userInterfaceLoader = new FXMLLoader(getClass().getResource(fxmlFilePath));

        userInterfaceLoader
                .setControllerFactory(controllerClassType -> buildControllerDynamically(controllerClassType));

        Parent rootVisualNode = userInterfaceLoader.load();
        Scene applicationScene = new Scene(rootVisualNode);

        applicationPrimaryStage.setScene(applicationScene);
        applicationPrimaryStage.setTitle(windowTitle);
        applicationPrimaryStage.show();
    }

    private Object buildControllerDynamically(Class<?> controllerClassType) {
        try {
            Constructor<?>[] availableConstructors = controllerClassType.getConstructors();

            for (Constructor<?> specificConstructor : availableConstructors) {
                if (specificConstructor.getParameterCount() == 1) {
                    Class<?> managerClassType = specificConstructor.getParameterTypes()[0];

                    try {
                        Constructor<?> managerConstructor = managerClassType
                                .getDeclaredConstructor(IDatabaseConnection.class, User.class);
                        Object managerInstance = managerConstructor.newInstance(databaseConnection, userInSession);

                        return specificConstructor.newInstance(managerInstance);

                    } catch (NoSuchMethodException constructorNotFoundException) {
                        Constructor<?> managerConstructor = managerClassType
                                .getDeclaredConstructor(IDatabaseConnection.class);
                        Object managerInstance = managerConstructor.newInstance(databaseConnection);

                        return specificConstructor.newInstance(managerInstance);
                    }
                }
            }

            return controllerClassType.getDeclaredConstructor().newInstance();
        } catch (Exception reflectionException) {
            throw new RuntimeException(reflectionException);
        }
    }

    public static void navigateToNextScene(ActionEvent userActionEvent, String fxmlFileLocation,
            String targetWindowTitle) throws java.io.IOException {
        Node userInterfaceNode = (Node) userActionEvent.getSource();
        Stage activeApplicationStage = (Stage) userInterfaceNode.getScene().getWindow();

        SceneManager activeSceneManager = (SceneManager) activeApplicationStage.getUserData();
        activeSceneManager.changeApplicationScene(fxmlFileLocation, targetWindowTitle);
    }

    public static void closeCurrentWindow(ActionEvent actionEvent) {
        Node eventSourceNode = (Node) actionEvent.getSource();
        Stage currentStage = (Stage) eventSourceNode.getScene().getWindow();
        currentStage.close();
    }
}