package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import mx.uv.fei.config.annotation.resolver.ConventionModuleResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ApplicationModuleFactory {

    public static IApplicationModule createModuleInstance(Class<?> targetEntryPointClass, String activeExecutionProfile) {
        IApplicationModule instantiatedModule = null;

        if (targetEntryPointClass.isAnnotationPresent(StartEtiquette.class)) {
            StartEtiquette startEtiquetteAnnotation = targetEntryPointClass.getAnnotation(StartEtiquette.class);
            Class<?> targetModuleClass = determineTargetModuleClass(targetEntryPointClass, startEtiquetteAnnotation);
            instantiatedModule = instantiateTargetModule(targetModuleClass, activeExecutionProfile);
        }

        return instantiatedModule;
    }

    private static Class<?> determineTargetModuleClass(Class<?> targetEntryPointClass, StartEtiquette startEtiquetteAnnotation) {
        Class<?> targetModuleClass = startEtiquetteAnnotation.factory();

        if (startEtiquetteAnnotation.factory().equals(IApplicationModule.class)) {
            targetModuleClass = ConventionModuleResolver.resolveByConvention(targetEntryPointClass);
        }

        return targetModuleClass;
    }

    private static IApplicationModule instantiateTargetModule(Class<?> targetModuleClass, String activeExecutionProfile) {
        IApplicationModule instantiatedModule;

        try {
            Constructor<?> profileConstructor = targetModuleClass.getDeclaredConstructor(String.class);
            profileConstructor.setAccessible(true);
            instantiatedModule = (IApplicationModule) profileConstructor.newInstance(activeExecutionProfile);
        } catch (NoSuchMethodException noSuchMethodException) {
            instantiatedModule = instantiateWithDefaultConstructor(targetModuleClass);
        } catch (InstantiationException instantiationException) {
            throw new IllegalStateException("No se puede instanciar la clase del modulo porque es abstracta o una interfaz: " + targetModuleClass.getName(), instantiationException);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException("El constructor del modulo de configuracion no es accesible publicamente: " + targetModuleClass.getName(), illegalAccessException);
        } catch (InvocationTargetException invocationTargetException) {
            throw new IllegalStateException("El constructor del modulo de configuracion arrojo una excepcion interna durante la instanciacion: " + targetModuleClass.getName(), invocationTargetException.getCause());
        }

        return instantiatedModule;
    }

    private static IApplicationModule instantiateWithDefaultConstructor(Class<?> targetModuleClass) {
        IApplicationModule instantiatedModule;

        try {
            Constructor<?> defaultConstructor = targetModuleClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            instantiatedModule = (IApplicationModule) defaultConstructor.newInstance();
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new IllegalStateException("No se encontro un constructor por defecto sin parametros para la clase del modulo: " + targetModuleClass.getName(), noSuchMethodException);
        } catch (InstantiationException instantiationException) {
            throw new IllegalStateException("No se puede instanciar la clase del modulo con el constructor por defecto porque es abstracta: " + targetModuleClass.getName(), instantiationException);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException("El constructor por defecto del modulo no es accesible publicamente: " + targetModuleClass.getName(), illegalAccessException);
        } catch (InvocationTargetException invocationTargetException) {
            throw new IllegalStateException("El constructor por defecto del modulo arrojo una excepcion interna durante la instanciacion: " + targetModuleClass.getName(), invocationTargetException.getCause());
        }

        return instantiatedModule;
    }
}