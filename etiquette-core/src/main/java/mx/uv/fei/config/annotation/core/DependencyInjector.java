package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyInjector {

    private final Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();
    private final ProviderMethodRegistry providerRegistry;
    private final IApplicationModule applicationModule;

    public DependencyInjector(IApplicationModule applicationModule) {
        this.applicationModule = applicationModule;
        this.providerRegistry = new ProviderMethodRegistry(applicationModule);
    }

    public <T> T retrieveInstance(Class<T> type) {
        return type.cast(resolveRecursively(type, applicationModule.retrieveGlobalProfile()));
    }

    public void injectDependencies(Object targetInstance) {
        injectFields(targetInstance, targetInstance.getClass(), resolveProfile(targetInstance.getClass()));
    }

    private Object resolveRecursively(Class<?> type, String profile) {
        if (singletonInstances.containsKey(type)) {
            return singletonInstances.get(type);
        }

        if (providerRegistry.contains(type)) {
            Object instance = providerRegistry.provide(type);
            singletonInstances.put(type, instance);
            return instance;
        }

        if (type.isInterface()) {
            throw new IllegalArgumentException("No se puede instanciar directamente la interfaz: " + type.getName()
                    + ". Asegúrate de proveer una implementación.");
        }

        profile = resolveProfile(type, profile);
        Constructor<?> constructor = selectConstructor(type);
        Object[] parameters = resolveParameters(constructor.getParameters(), profile);

        try {
            Object instance = constructor.newInstance(parameters);

            injectFields(instance, type, profile);

            if (type.isAnnotationPresent(Component.class)) {
                registerSingleton(type, instance);
            }

            return instance;

        } catch (InstantiationException exception) {
            throw new IllegalStateException("No se puede instanciar la clase (podría ser abstracta): " + type.getName(),
                    exception);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("El constructor de la clase no es accesible: " + type.getName(), exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("El constructor de la clase arrojó una excepción: " + type.getName(),
                    exception.getCause());
        }
    }

    private Object[] resolveParameters(Parameter[] parameters, String profile) {
        Object[] resolvedParameters = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == String.class) {
                resolvedParameters[i] = profile;
            } else {
                resolvedParameters[i] = resolveRecursively(parameters[i].getType(), profile);
            }
        }

        return resolvedParameters;
    }

    private void injectFields(Object target, Class<?> type, String profile) {
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object dependency = resolveRecursively(field.getType(), profile);

                try {
                    field.set(target, dependency);
                } catch (IllegalAccessException exception) {
                    throw new IllegalStateException(
                            "No se pudo inyectar la dependencia en el campo: " + field.getName(), exception);
                }
            }
        }
    }

    private Constructor<?> selectConstructor(Class<?> type) {
        for (Constructor<?> constructor : type.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }

        Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length == 0) {
            throw new IllegalStateException(
                    "No se encontró ningún constructor público para la clase: " + type.getName());
        }
        return constructors[0];
    }

    private void registerSingleton(Class<?> type, Object instance) {
        singletonInstances.put(type, instance);

        for (Class<?> implementedInterface : type.getInterfaces()) {
            singletonInstances.put(implementedInterface, instance);
        }
    }

    private String resolveProfile(Class<?> type) {
        return resolveProfile(type, applicationModule.retrieveGlobalProfile());
    }

    private String resolveProfile(Class<?> type, String fallback) {
        if (type.isAnnotationPresent(Profile.class)) {
            return type.getAnnotation(Profile.class).value();
        }
        return fallback;
    }
}