package mx.uv.fei.config.annotation;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.core.ApplicationConfigurationFactory;
import mx.uv.fei.config.annotation.core.DependencyInjector;

public class EtiquetteApplication {

    public static DependencyInjector run(Class<?> entryPointClass, IApplicationModule module) {
        return new DependencyInjector(module);
    }

    public static IApplicationModule bootstrap(String profile) {
        // Llama a la factoría para obtener el módulo configurado
        return ApplicationConfigurationFactory.create(profile);
    }
}