package mx.uv.fei.config;

import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.manager.RegisterAdminManager;
import mx.uv.fei.presentation.MainController;

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
                    Constructor<?> managerConstructor = managerClass.getDeclaredConstructor(IDatabaseConnection.class);
                    Object managerInstance = managerConstructor.newInstance(this.dbConnection);

                    return constructor.newInstance(managerInstance);
                }
            }

            return controllerClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Error fatal de Inyección de Dependencias al construir: " + controllerClass.getName(), e);
        }
    }

    // Método auxiliar mantenido para la carga inicial del MainController
    public RegisterAdminManager getRegisterAdminManager() {
        return new RegisterAdminManager(this.dbConnection);
    }
}