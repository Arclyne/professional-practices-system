package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;
import mx.uv.fei.config.annotation.resolver.ConventionModuleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class ApplicationModuleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationModuleFactory.class);

    public static IApplicationModule createModuleInstance(Class<?> targetEntryPointClass, String activeExecutionProfile) {
        IApplicationModule instantiatedModule = null;

        if (targetEntryPointClass.isAnnotationPresent(StartEtiquette.class)) {
            StartEtiquette startEtiquetteAnnotation = targetEntryPointClass.getAnnotation(StartEtiquette.class);
            Class<?> targetModuleClass = resolveModuleClass(targetEntryPointClass, startEtiquetteAnnotation);
            instantiatedModule = instantiateModule(targetModuleClass, activeExecutionProfile);
        }

        return instantiatedModule;
    }

    private static Class<?> resolveModuleClass(Class<?> targetEntryPointClass, StartEtiquette startEtiquette) {
        boolean usesDefaultFactory = startEtiquette.factory().equals(IApplicationModule.class);
        Class<?> resolvedModuleClass;

        if (usesDefaultFactory) {
            resolvedModuleClass = ConventionModuleResolver.resolveByConvention(targetEntryPointClass);
        } else {
            resolvedModuleClass = startEtiquette.factory();
        }

        return resolvedModuleClass;
    }

    private static IApplicationModule instantiateModule(Class<?> targetModuleClass, String activeExecutionProfile) {
        IApplicationModule instantiatedModule;

        try {
            Constructor<?> profileConstructor = targetModuleClass.getDeclaredConstructor(String.class);
            profileConstructor.setAccessible(true);
            instantiatedModule = (IApplicationModule) profileConstructor.newInstance(activeExecutionProfile);
        } catch (NoSuchMethodException e) {
            instantiatedModule = instantiateWithDefaultConstructor(targetModuleClass);
        } catch (InstantiationException e) {
            LOG.error("No se puede instanciar el modulo porque es abstracto o una interfaz: {}", targetModuleClass.getName(), e);
            throw new IllegalStateException("No se puede instanciar el modulo porque es abstracto o una interfaz: " + targetModuleClass.getName(), e);
        } catch (IllegalAccessException e) {
            LOG.error("El constructor del modulo no es accesible: {}", targetModuleClass.getName(), e);
            throw new IllegalStateException("El constructor del modulo no es accesible: " + targetModuleClass.getName(), e);
        } catch (InvocationTargetException e) {
            LOG.error("El constructor del modulo lanzo una excepcion interna durante la instanciacion: {}", targetModuleClass.getName(), e.getCause());
            throw new IllegalStateException("El constructor del modulo lanzo una excepcion interna durante la instanciacion: " + targetModuleClass.getName(), e.getCause());
        }

        return instantiatedModule;
    }

    private static IApplicationModule instantiateWithDefaultConstructor(Class<?> targetModuleClass) {
        IApplicationModule instantiatedModule;

        try {
            Constructor<?> defaultConstructor = targetModuleClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            instantiatedModule = (IApplicationModule) defaultConstructor.newInstance();
        } catch (NoSuchMethodException e) {
            LOG.error("No se encontro un constructor sin parametros para: {}", targetModuleClass.getName(), e);
            throw new IllegalStateException("No se encontro un constructor sin parametros para: " + targetModuleClass.getName(), e);
        } catch (InstantiationException e) {
            LOG.error("No se puede instanciar con el constructor por defecto porque es abstracta: {}", targetModuleClass.getName(), e);
            throw new IllegalStateException("No se puede instanciar con el constructor por defecto porque es abstracta: " + targetModuleClass.getName(), e);
        } catch (IllegalAccessException e) {
            LOG.error("El constructor por defecto no es accesible en: {}", targetModuleClass.getName(), e);
            throw new IllegalStateException("El constructor por defecto no es accesible en: " + targetModuleClass.getName(), e);
        } catch (InvocationTargetException e) {
            LOG.error("El constructor por defecto lanzo una excepcion interna en: {}", targetModuleClass.getName(), e.getCause());
            throw new IllegalStateException("El constructor por defecto lanzo una excepcion interna en: " + targetModuleClass.getName(), e.getCause());
        }

        return instantiatedModule;
    }
}
