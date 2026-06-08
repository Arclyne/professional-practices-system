package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.profile.ExecutionProfileResolver;
import mx.uv.fei.config.annotation.resolver.BasePackageResolver;


public class EtiquetteApplication {

    public static DependencyInjector run(Class<?> targetEntryPointClass) {
        String activeExecutionProfile = ExecutionProfileResolver.resolveProfile(targetEntryPointClass);
        IApplicationModule applicationModule = ApplicationModuleFactory.createModuleInstance(targetEntryPointClass, activeExecutionProfile);
        String basePackage = BasePackageResolver.calculateBasePackage(targetEntryPointClass);
        ComponentScanner componentScanner = new ComponentScanner(basePackage);
        DependencyInjector dependencyInjector = new DependencyInjector(applicationModule, componentScanner);

        EagerInstanceInitializer eagerInitializer = new EagerInstanceInitializer(
                dependencyInjector,
                componentScanner.getCatalog(),
                activeExecutionProfile
        );
        eagerInitializer.initialize();

        return dependencyInjector;
    }
}
