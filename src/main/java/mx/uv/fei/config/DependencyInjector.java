package mx.uv.fei.config;

import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.manager.RegisterAdminManager;
import mx.uv.fei.presentation.MainController;
import mx.uv.fei.domain.statemachine.Store;

import java.lang.reflect.Constructor;

public class DependencyInjector {

    private final IDatabaseConnection dbConnection;

    public DependencyInjector(IDatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Object getController(Class<?> controllerClass) {
        if (controllerClass == MainController.class) {
            return new MainController(this);
        }

        try {
            Constructor<?>[] constructors = controllerClass.getConstructors();

            for (Constructor<?> constructor : constructors) {

                if (constructor.getParameterCount() == 1) {
                    Class<?> managerClass = constructor.getParameterTypes()[0];
                    Constructor<?>[] managerConstructors = managerClass.getConstructors();

                    for (Constructor<?> managerConstructor : managerConstructors) {

                        if (managerConstructor.getParameterCount() == 1
                                && managerConstructor.getParameterTypes()[0] == IDatabaseConnection.class) {
                            Object managerInstance = managerConstructor.newInstance(this.dbConnection);
                            return constructor.newInstance(managerInstance);
                        }

                        else if (managerConstructor.getParameterCount() == 2 &&
                                managerConstructor.getParameterTypes()[0] == IDatabaseConnection.class &&
                                managerConstructor.getParameterTypes()[1] == User.class) {

                            User currentUser = Store.getInstance().getState().authState().currentUser();

                            Object managerInstance = managerConstructor.newInstance(this.dbConnection, currentUser);
                            return constructor.newInstance(managerInstance);
                        }
                    }
                }
            }

            return controllerClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error fatal de Inyección de Dependencias al construir: " + controllerClass.getName(), e);
        }
    }

    public RegisterAdminManager getRegisterAdminManager() {
        return new RegisterAdminManager(this.dbConnection);
    }
}