package mx.uv.fei.config.annotation;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.StartEtiquette;

public class EtiquetteApplication {

    public static DependencyInjector run(Class<?> entryPointClass, IApplicationModule module) {
        return new DependencyInjector(module);
    }

    public static String resolveProfile(Class<?> entryPointClass) {
        if (entryPointClass.isAnnotationPresent(StartEtiquette.class)) {
            return entryPointClass.getAnnotation(StartEtiquette.class).profile();
        }
        return "local";
    }
}