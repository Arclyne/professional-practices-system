package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.provider.ProviderMethodRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DependencyInjector {

    private static final Logger LOG = LoggerFactory.getLogger(DependencyInjector.class);

    private final Map<Class<?>, Object> singletonPool = new ConcurrentHashMap<>();
    private final ComponentScanner componentScanner;
    private final ProviderMethodRegistry providerRegistry;
    private final IApplicationModule applicationModule;

    public DependencyInjector(IApplicationModule applicationModule, ComponentScanner componentScanner) {
        this.applicationModule = applicationModule;
        this.componentScanner = componentScanner;
        this.providerRegistry = new ProviderMethodRegistry(applicationModule);
    }

    public Object resolve(Class<?> type) {
        Object resolvedInstance;

        if (type == IApplicationModule.class) {
            resolvedInstance = applicationModule;
        } else if (type == DependencyInjector.class) {
            resolvedInstance = this;
        } else if (singletonPool.containsKey(type)) {
            resolvedInstance = singletonPool.get(type);
        } else if (providerRegistry.contains(type)) {
            resolvedInstance = providerRegistry.provide(type);
        } else {
            resolvedInstance = resolveFromScanner(type);
        }

        return resolvedInstance;
    }


    public Object retrieveInstance(Class<?> controllerType) {
        return resolve(controllerType);
    }

    public boolean isRegistered(Class<?> type) {
        return singletonPool.containsKey(type);
    }

    public void registerKeepInstance(Class<?> type, Object instance) {
        singletonPool.put(type, instance);

        for (Class<?> iface : type.getInterfaces()) {
            singletonPool.put(iface, instance);
        }
    }


    public void registerAdditionalProviders(Object keepInstance) {
        providerRegistry.registerProvidersFrom(keepInstance);
    }

    public void injectDependencies(Object targetInstance) {
        for (Field field : targetInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                injectField(targetInstance, field);
            }
        }
    }

    private Object resolveFromScanner(Class<?> type) {
        Class<?> implementation = componentScanner.getImpl(type);

        if (implementation == null) {
            throw new IllegalArgumentException("No hay proveedor registrado para el tipo: " + type.getName());
        }

        return instantiate(implementation);
    }

    private Object instantiate(Class<?> implementation) {
        Constructor<?> constructor = implementation.getDeclaredConstructors()[0];
        Object[] resolvedArguments = resolveConstructorArguments(constructor);
        Object instance;

        try {
            instance = constructor.newInstance(resolvedArguments);
            injectDependencies(instance);
            registerAsSingletonIfComponent(implementation, instance);
        } catch (InstantiationException e) {
            LOG.error("No se puede instanciar la clase porque es abstracta: {}", implementation.getName(), e);
            throw new IllegalStateException("No se puede instanciar la clase porque es abstracta: " + implementation.getName(), e);
        } catch (IllegalAccessException e) {
            LOG.error("El constructor no es accesible para la clase: {}", implementation.getName(), e);
            throw new IllegalStateException("El constructor no es accesible para la clase: " + implementation.getName(), e);
        } catch (InvocationTargetException e) {
            LOG.error("El constructor lanzo una excepcion interna al instanciar: {}", implementation.getName(), e.getCause());
            throw new IllegalStateException("El constructor lanzo una excepcion interna al instanciar: " + implementation.getName(), e.getCause());
        }

        return instance;
    }

    private Object[] resolveConstructorArguments(Constructor<?> constructor) {
        Object[] resolvedArguments = new Object[constructor.getParameterCount()];

        for (int i = 0; i < resolvedArguments.length; i++) {
            resolvedArguments[i] = resolve(constructor.getParameterTypes()[i]);
        }

        return resolvedArguments;
    }

    private void injectField(Object targetInstance, Field field) {
        field.setAccessible(true);

        try {
            field.set(targetInstance, resolve(field.getType()));
        } catch (IllegalAccessException e) {
            LOG.error("No se pudo inyectar el campo {} en: {}", field.getName(), targetInstance.getClass().getName(), e);
            throw new IllegalStateException("No se pudo inyectar el campo " + field.getName() + " en: " + targetInstance.getClass().getName(), e);
        }
    }

    private void registerAsSingletonIfComponent(Class<?> implementation, Object instance) {
        if (implementation.isAnnotationPresent(Component.class)) {
            singletonPool.put(implementation, instance);

            for (Class<?> iface : implementation.getInterfaces()) {
                singletonPool.put(iface, instance);
            }
        }
    }
}
