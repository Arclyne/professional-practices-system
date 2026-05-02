package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.config.annotation.etiquette.Profile;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyInjector {

    private final Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();
    // Catálogo de clases descubiertas por el escáner (Interfaz -> Implementación)
    private final Map<Class<?>, Class<?>> componentCatalog = new ConcurrentHashMap<>();
    private final ProviderMethodRegistry providerRegistry;
    private final IApplicationModule applicationModule;

    public DependencyInjector(IApplicationModule applicationModule, String basePackage) {
        this.applicationModule = applicationModule;
        this.providerRegistry = new ProviderMethodRegistry(applicationModule);

        scanPackages(basePackage);

        initializeKeepComponents();
    }

    private void scanPackages(String basePackage) {
        try {
            List<Class<?>> classes = PackageScanner.findClasses(basePackage);
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Component.class)) {

                    componentCatalog.put(clazz, clazz);
                    for (Class<?> iface : clazz.getInterfaces()) {
                        componentCatalog.put(iface, clazz);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Advertencia: Error escaneando paquetes en " + basePackage + " -> " + e.getMessage());
        }
    }

    private void initializeKeepComponents() {
        for (Class<?> clazz : componentCatalog.values()) {
            if (clazz.isAnnotationPresent(Keep.class) && !singletonInstances.containsKey(clazz)) {
                retrieveInstance(clazz);
            }
        }
    }

    public <T> T retrieveInstance(Class<T> type) {
        String profile = (applicationModule != null) ? applicationModule.retrieveGlobalProfile() : "local";
        return type.cast(resolveRecursively(type, profile));
    }

    public void injectDependencies(Object targetInstance) {
        injectFields(targetInstance, targetInstance.getClass(), resolveProfile(targetInstance.getClass()));
    }

    private Object resolveRecursively(Class<?> type, String profile) {
        if (type == IApplicationModule.class) {
            return applicationModule;
        }

        if (type == DependencyInjector.class) {
            return this;
        }

        if (singletonInstances.containsKey(type)) {
            return singletonInstances.get(type);
        }

        if (providerRegistry.contains(type)) {
            Object instance = providerRegistry.provide(type);
            singletonInstances.put(type, instance);
            return instance;
        }

        Class<?> implementationClass = componentCatalog.get(type);
        if (implementationClass != null) {
            type = implementationClass;
        } else if (type.isInterface()) {
            throw new IllegalArgumentException("No hay provider ni componente registrado en el catálogo para la interfaz: " + type.getName());
        }

        profile = resolveProfile(type, profile);
        Constructor<?> constructor = selectConstructor(type);
        Object[] parameters = resolveParameters(constructor.getParameters(), profile);

        try {
            Object instance = constructor.newInstance(parameters);

            injectFields(instance, type, profile);

            if (type.isAnnotationPresent(Component.class)) {
                registerSingleton(type, instance);
                providerRegistry.registerDynamicProviders(instance);
            }

            return instance;

        } catch (InstantiationException exception) {
            throw new IllegalStateException("No se puede instanciar la clase (podría ser abstracta): " + type.getName(), exception);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("El constructor de la clase no es accesible: " + type.getName(), exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("El constructor de la clase arrojó una excepción: " + type.getName(), exception.getCause());
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
                    throw new IllegalStateException("No se pudo inyectar la dependencia en el campo: " + field.getName(), exception);
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
            throw new IllegalStateException("No se encontró ningún constructor público para la clase: " + type.getName());
        }
        return constructors[0];
    }

    private void registerSingleton(Class<?> type, Object instance) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            singletonInstances.put(current, instance);
            for (Class<?> iface : current.getInterfaces()) {
                singletonInstances.put(iface, instance);
            }
            current = current.getSuperclass();
        }
    }

    private String resolveProfile(Class<?> type) {
        String globalProfile = (applicationModule != null) ? applicationModule.retrieveGlobalProfile() : "local";
        return resolveProfile(type, globalProfile);
    }

    private String resolveProfile(Class<?> type, String fallback) {
        if (type.isAnnotationPresent(Profile.class)) {
            return type.getAnnotation(Profile.class).value();
        }
        return fallback;
    }
}