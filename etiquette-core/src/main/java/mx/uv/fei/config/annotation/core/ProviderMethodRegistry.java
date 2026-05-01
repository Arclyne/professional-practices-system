package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProviderMethodRegistry {

    private final Map<Class<?>, Method> providerMethods = new HashMap<>();
    private final Object moduleInstance;

    public ProviderMethodRegistry(Object moduleInstance) {
        this.moduleInstance = moduleInstance;
        registerProviderMethods(moduleInstance);
    }

    private void registerProviderMethods(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(Provide.class)) {
                providerMethods.put(method.getReturnType(), method);
            }
        }
    }

    public boolean contains(Class<?> type) {
        return providerMethods.containsKey(type);
    }

    public Object provide(Class<?> type) {
        Method method = providerMethods.get(type);

        try {
            return method.invoke(moduleInstance);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "No se tiene acceso al método proveedor de la dependencia: " + type.getName(), exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("El método proveedor falló al crear la dependencia: " + type.getName(),
                    exception.getCause());
        }
    }
}