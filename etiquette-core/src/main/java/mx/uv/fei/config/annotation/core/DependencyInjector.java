package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.config.annotation.provider.ProviderMethodRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyInjector {

    private final Map<Class<?>, Object> singletonInstancesMap = new ConcurrentHashMap<>();
    private final ProviderMethodRegistry systemProviderRegistry;
    private final IApplicationModule currentApplicationModule;
    private final ComponentScanner systemComponentScanner;

    public DependencyInjector(IApplicationModule applicationModule, String basePackageToScan) {
        this.currentApplicationModule = applicationModule;
        this.systemProviderRegistry = new ProviderMethodRegistry(applicationModule);
        this.systemComponentScanner = new ComponentScanner(basePackageToScan);

        initializeKeepComponents();
    }

    private void initializeKeepComponents() {
        for (Class<?> currentComponentClass : systemComponentScanner.getCatalogValues()) {
            if (currentComponentClass.isAnnotationPresent(Keep.class) && !singletonInstancesMap.containsKey(currentComponentClass)) {
                retrieveInstance(currentComponentClass);
            }
        }
    }

    public <TargetType> TargetType retrieveInstance(Class<TargetType> targetType) {
        String activeExecutionProfile = getGlobalProfile();
        return targetType.cast(resolveRecursively(targetType, activeExecutionProfile));
    }

    public void injectDependencies(Object targetInstance) {
        String activeExecutionProfile = ReflectionHelper.resolveProfile(targetInstance.getClass(), getGlobalProfile());
        injectFields(targetInstance, targetInstance.getClass(), activeExecutionProfile);
    }

    private Object resolveRecursively(Class<?> targetType, String activeExecutionProfile) {
        Object resolvedDependencyInstance;

        if (targetType == IApplicationModule.class) {
            resolvedDependencyInstance = currentApplicationModule;
        } else if (targetType == DependencyInjector.class) {
            resolvedDependencyInstance = this;
        } else if (singletonInstancesMap.containsKey(targetType)) {
            resolvedDependencyInstance = singletonInstancesMap.get(targetType);
        } else if (systemProviderRegistry.contains(targetType)) {
            resolvedDependencyInstance = systemProviderRegistry.provide(targetType);
            singletonInstancesMap.put(targetType, resolvedDependencyInstance);
        } else {
            Class<?> implementationClass = systemComponentScanner.getImplementation(targetType);
            if (implementationClass != null) {
                targetType = implementationClass;
            } else if (targetType.isInterface()) {
                throw new IllegalArgumentException("No se encontro un proveedor ni un componente registrado en el catalogo para satisfacer la interfaz solicitada: " + targetType.getName());
            }

            activeExecutionProfile = ReflectionHelper.resolveProfile(targetType, activeExecutionProfile);
            Constructor<?> selectedConstructor = ReflectionHelper.selectConstructor(targetType);
            Object[] resolvedConstructorParametersArray = resolveParameters(selectedConstructor.getParameters(), activeExecutionProfile);

            resolvedDependencyInstance = instantiateAndRegister(targetType, selectedConstructor, resolvedConstructorParametersArray, activeExecutionProfile);
        }

        return resolvedDependencyInstance;
    }

    private Object instantiateAndRegister(Class<?> targetType, Constructor<?> targetConstructor, Object[] constructorParameters, String activeExecutionProfile) {
        Object newInstantiatedObject;

        try {
            newInstantiatedObject = targetConstructor.newInstance(constructorParameters);

            injectFields(newInstantiatedObject, targetType, activeExecutionProfile);

            if (targetType.isAnnotationPresent(Component.class)) {
                registerSingleton(targetType, newInstantiatedObject);
                systemProviderRegistry.registerDynamicProviders(newInstantiatedObject);
            }

        } catch (InstantiationException instantiationException) {
            throw new IllegalStateException("No fue posible instanciar la clase solicitada, asegurese de que no sea una clase abstracta: " + targetType.getName(), instantiationException);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException("El constructor de la clase solicitada no es accesible publicamente: " + targetType.getName(), illegalAccessException);
        } catch (InvocationTargetException invocationTargetException) {
            throw new IllegalStateException("El constructor de la clase solicitada arrojo una excepcion interna durante su ejecucion: " + targetType.getName(), invocationTargetException.getCause());
        }

        return newInstantiatedObject;
    }

    private Object[] resolveParameters(Parameter[] constructorParametersArray, String activeExecutionProfile) {
        Object[] resolvedParametersArray = new Object[constructorParametersArray.length];

        for (int parameterIndex = 0; parameterIndex < constructorParametersArray.length; parameterIndex++) {
            if (constructorParametersArray[parameterIndex].getType() == String.class) {
                resolvedParametersArray[parameterIndex] = activeExecutionProfile;
            } else {
                resolvedParametersArray[parameterIndex] = resolveRecursively(constructorParametersArray[parameterIndex].getType(), activeExecutionProfile);
            }
        }

        return resolvedParametersArray;
    }

    private void injectFields(Object targetInstance, Class<?> targetType, String activeExecutionProfile) {
        for (Field currentClassField : targetType.getDeclaredFields()) {
            if (currentClassField.isAnnotationPresent(Inject.class)) {
                currentClassField.setAccessible(true);
                Object resolvedFieldDependency = resolveRecursively(currentClassField.getType(), activeExecutionProfile);
                try {
                    currentClassField.set(targetInstance, resolvedFieldDependency);
                } catch (IllegalAccessException illegalAccessException) {
                    throw new IllegalStateException("Ocurrio un error al intentar inyectar la dependencia requerida en el campo: " + currentClassField.getName(), illegalAccessException);
                }
            }
        }
    }

    private void registerSingleton(Class<?> targetType, Object singletonInstanceToRegister) {
        Class<?> currentClassInHierarchy = targetType;

        while (currentClassInHierarchy != null && currentClassInHierarchy != Object.class) {
            singletonInstancesMap.put(currentClassInHierarchy, singletonInstanceToRegister);
            for (Class<?> implementedInterface : currentClassInHierarchy.getInterfaces()) {
                singletonInstancesMap.put(implementedInterface, singletonInstanceToRegister);
            }
            currentClassInHierarchy = currentClassInHierarchy.getSuperclass();
        }
    }

    private String getGlobalProfile() {
        String globalExecutionProfile = "local";

        if (currentApplicationModule != null) {
            globalExecutionProfile = currentApplicationModule.retrieveGlobalProfile();
        }

        return globalExecutionProfile;
    }
}