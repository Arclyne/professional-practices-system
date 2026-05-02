package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderMethodRegistry {

    private final Map<Class<?>, Method> providerMethods = new HashMap<>();
    private final Map<Method, Object> instanceMap = new ConcurrentHashMap<>();

    public ProviderMethodRegistry(Object moduleInstance) {
        if (moduleInstance != null) {
            registerDynamicProviders(moduleInstance);
        }
    }


    public boolean contains(Class<?> type) {
        return providerMethods.containsKey(type);
    }

    public Object provide(Class<?> type) {
        Method method = providerMethods.get(type);
        Object target = instanceMap.get(method);

        if (target == null) {
            throw new IllegalStateException("No se encontró una instancia válida para el proveedor de: " + type.getName());
        }

        try {
            return method.invoke(target);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Error al invocar el proveedor para: " + type.getName(), exception);
        }
    }

    public void registerDynamicProviders(Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Provide.class)) {
                method.setAccessible(true);
                providerMethods.put(method.getReturnType(), method);
                instanceMap.put(method, instance);
            }
        }
    }
}
