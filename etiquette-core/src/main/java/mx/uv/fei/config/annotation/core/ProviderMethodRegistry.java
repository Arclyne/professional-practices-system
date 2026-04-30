package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.etiquette.Provide;

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

    public Object provide(Class<?> type) throws Exception {
        Method method = providerMethods.get(type);
        return method.invoke(moduleInstance);
    }
}